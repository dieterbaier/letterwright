package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Vorlagenversion;
import dieterbaier.standardschreiben.usecases.in.SchreibenSpeichern;
import dieterbaier.standardschreiben.usecases.in.Speicherergebnis;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.DokumentAblage;
import dieterbaier.standardschreiben.usecases.out.Konformitaet;
import dieterbaier.standardschreiben.usecases.out.KonformitaetsPruefer;
import dieterbaier.standardschreiben.usecases.out.Platzhalterdatenquelle;
import dieterbaier.standardschreiben.usecases.out.Renderer;
import dieterbaier.standardschreiben.usecases.out.SchreibenRepository;
import dieterbaier.standardschreiben.usecases.out.Snapshotablage;
import dieterbaier.standardschreiben.usecases.out.VorlagenRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Prüft Snapshot und Berechtigung, bevor es nach einem vorhandenen Schreiben sucht: Ein Unberechtigter erfährt so
 * nicht, ob zu einem Snapshot schon gespeichert wurde. Erst danach liest es den Datenkontext erneut, erzeugt das PDF
 * und speichert nur ein konformes (fail-closed). Was abgelegt, aber nicht gespeichert wurde, entfernt es wieder.
 */
public final class Schreibenspeicherung implements SchreibenSpeichern {

    private final Snapshotablage snapshots;
    private final VorlagenRepository vorlagen;
    private final Berechtigungen berechtigungen;
    private final Platzhalterdatenquelle daten;
    private final Renderer renderer;
    private final KonformitaetsPruefer pruefer;
    private final DokumentAblage dokumente;
    private final SchreibenRepository schreiben;
    private final Clock uhr;

    public Schreibenspeicherung(Snapshotablage snapshots, VorlagenRepository vorlagen, Berechtigungen berechtigungen,
            Platzhalterdatenquelle daten, Renderer renderer, KonformitaetsPruefer pruefer, DokumentAblage dokumente,
            SchreibenRepository schreiben, Clock uhr) {
        this.snapshots = snapshots;
        this.vorlagen = vorlagen;
        this.berechtigungen = berechtigungen;
        this.daten = daten;
        this.renderer = renderer;
        this.pruefer = pruefer;
        this.dokumente = dokumente;
        this.schreiben = schreiben;
        this.uhr = uhr;
    }

    @Override
    public Speicherergebnis speichere(BenutzerKennung benutzer, SnapshotId id) {
        Optional<Snapshot> eigener = snapshots.lade(id).filter(snapshot -> snapshot.benutzer().equals(benutzer));
        if (eigener.isEmpty()) {
            return new Speicherergebnis.SnapshotUnbekannt();
        }
        Snapshot snapshot = eigener.get();
        Instant jetzt = uhr.instant();
        if (snapshot.istAbgelaufen(jetzt)) {
            return new Speicherergebnis.SnapshotAbgelaufen();
        }
        Standardschreiben vorlage = vorlagen.standardschreiben(snapshot.standardschreiben().vorlage())
                .orElseThrow(() -> fehlt(snapshot.standardschreiben()));
        if (!berechtigungen.darfErzeugen(benutzer, snapshot.bezug(), vorlage)) {
            return new Speicherergebnis.NichtErlaubt();
        }
        Optional<Schreiben> vorhanden = schreiben.zuSnapshot(id);
        if (vorhanden.isPresent()) {
            return new Speicherergebnis.BereitsGespeichert(vorhanden.get().id());
        }
        Datenkontext neuGelesen = daten.liesDatenkontext(benutzer, snapshot.bezug(), vorlage);
        if (!snapshot.hatUnveraendertenDatenkontext(neuGelesen)) {
            return new Speicherergebnis.DatenkontextGeaendert();
        }
        byte[] pdf = renderer.erzeugePdf(briefpapierVersion(snapshot), vorlage.version(snapshot.standardschreiben()
                .nummer()).orElseThrow(() -> fehlt(snapshot.standardschreiben())), snapshot.datenkontext());
        return switch (pruefer.pruefe(pdf)) {
            case Konformitaet.NichtKonform nichtKonform -> new Speicherergebnis.NichtKonform(nichtKonform.befunde());
            case Konformitaet.Konform konform -> speichere(snapshot, pdf, jetzt);
        };
    }

    private Speicherergebnis speichere(Snapshot snapshot, byte[] pdf, Instant jetzt) {
        Dokumentverweis dokument = dokumente.lege(pdf);
        Schreiben neu;
        Schreiben gespeichert;
        try {
            neu = Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, Pruefsumme.ueber(pdf), jetzt);
            gespeichert = schreiben.speichereEinmalig(neu);
        } catch (RuntimeException fehler) {
            dokumente.entferne(dokument);
            throw fehler;
        }
        if (!gespeichert.equals(neu)) {
            // Eine gleichzeitige Anfrage zum selben Snapshot war schneller; ihr PDF gilt.
            dokumente.entferne(dokument);
            return new Speicherergebnis.BereitsGespeichert(gespeichert.id());
        }
        return new Speicherergebnis.Gespeichert(gespeichert.id());
    }

    private Vorlagenversion briefpapierVersion(Snapshot snapshot) {
        Versionsverweis verweis = snapshot.briefpapier();
        return vorlagen.briefpapier(verweis.vorlage()).flatMap(briefpapier -> briefpapier.version(verweis.nummer()))
                .orElseThrow(() -> fehlt(verweis));
    }

    /** Versionen sind unveränderlich und werden nie gelöscht (ADR-007); fehlt eine, ist der Bestand defekt. */
    private static IllegalStateException fehlt(Versionsverweis verweis) {
        return new IllegalStateException("Vorlagenversion " + verweis + " aus einem Snapshot fehlt");
    }
}
