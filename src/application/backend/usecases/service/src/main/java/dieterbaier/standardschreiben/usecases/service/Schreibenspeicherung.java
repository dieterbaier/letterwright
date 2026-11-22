package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenAusSnapshot;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshotzustand;
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
import dieterbaier.standardschreiben.usecases.out.Speicherausgang;
import dieterbaier.standardschreiben.usecases.out.Snapshotablage;
import dieterbaier.standardschreiben.usecases.out.VorlagenRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Prüft Snapshot und Berechtigung, bevor es den Zustand des Snapshots auswertet: Ein Unberechtigter erfährt so nicht,
 * ob schon gespeichert wurde. Aus einem offenen Snapshot liest es den Datenkontext erneut, erzeugt das PDF und speichert
 * nur ein konformes (fail-closed). Ein PDF entfernt es nur, wenn sicher ist, dass kein Schreiben darauf verweist.
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
        return switch (snapshot.zustand()) {
            // Ist das Schreiben inzwischen gelöscht, gibt es für den Benutzer nichts mehr zu finden.
            case Snapshotzustand.Verbraucht verbraucht -> schreiben.zuSnapshot(id)
                    .filter(Schreiben::istSichtbar)
                    .<Speicherergebnis>map(vorhandenes -> new Speicherergebnis.BereitsGespeichert(vorhandenes.id()))
                    .orElseGet(Speicherergebnis.SnapshotUnbekannt::new);
            case Snapshotzustand.Offen offen -> speichere(benutzer, snapshot, offen, vorlage, jetzt);
        };
    }

    private Speicherergebnis speichere(BenutzerKennung benutzer, Snapshot snapshot, Snapshotzustand.Offen offen,
            Standardschreiben vorlage, Instant jetzt) {
        if (!offen.istUnveraendert(daten.liesDatenkontext(benutzer, snapshot.bezug(), vorlage))) {
            return new Speicherergebnis.DatenkontextGeaendert();
        }
        byte[] pdf = renderer.erzeugePdf(briefpapierVersion(snapshot), vorlage.version(snapshot.standardschreiben()
                .nummer()).orElseThrow(() -> fehlt(snapshot.standardschreiben())), offen.datenkontext());
        return switch (pruefer.pruefe(pdf)) {
            case Konformitaet.NichtKonform nichtKonform -> new Speicherergebnis.NichtKonform(nichtKonform.befunde());
            case Konformitaet.Konform konform -> speichere(snapshot, pdf, jetzt);
        };
    }

    private Speicherergebnis speichere(Snapshot snapshot, byte[] pdf, Instant jetzt) {
        Dokumentverweis dokument = dokumente.lege(pdf);
        SchreibenAusSnapshot neu =
                Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, Pruefsumme.ueber(pdf), jetzt);
        // Wirft speichere, ist der Ausgang ungewiss; das PDF bleibt, weil ein Schreiben darauf verweisen kann.
        return switch (schreiben.speichere(neu)) {
            case Speicherausgang.Gespeichert gespeichert -> new Speicherergebnis.Gespeichert(neu.schreiben().id());
            case Speicherausgang.SnapshotNichtMehrVorhanden entfernt -> {
                // Eine neue Vorschau hat den Snapshot inzwischen ersetzt; gespeichert wurde sicher nichts.
                dokumente.entferne(dokument);
                yield new Speicherergebnis.SnapshotUnbekannt();
            }
            case Speicherausgang.SnapshotBereitsVerbraucht bereits -> {
                // Eine gleichzeitige Anfrage war schneller; ihr PDF gilt, auf dieses verweist sicher nichts.
                dokumente.entferne(dokument);
                yield bereits.vorhandenes().istSichtbar()
                        ? new Speicherergebnis.BereitsGespeichert(bereits.vorhandenes().id())
                        : new Speicherergebnis.SnapshotUnbekannt();
            }
            case Speicherausgang.SchreibenEntfernt entfernt -> {
                // Das Schreiben der schnelleren Anfrage ist schon gelöscht; auf dieses PDF verweist sicher nichts.
                dokumente.entferne(dokument);
                yield new Speicherergebnis.SnapshotUnbekannt();
            }
        };
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
