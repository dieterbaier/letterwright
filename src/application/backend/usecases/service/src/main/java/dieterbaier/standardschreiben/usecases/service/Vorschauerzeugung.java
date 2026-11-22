package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Vorlagenversion;
import dieterbaier.standardschreiben.usecases.in.VorschauErzeugen;
import dieterbaier.standardschreiben.usecases.in.Vorschauergebnis;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.Platzhalterdatenquelle;
import dieterbaier.standardschreiben.usecases.out.Renderer;
import dieterbaier.standardschreiben.usecases.out.Snapshotablage;
import dieterbaier.standardschreiben.usecases.out.VorlagenRepository;
import java.time.Clock;
import java.util.Optional;

/**
 * Prüft erst, was die Bibliothek selbst weiß (Standardschreiben, Bezugstyp, veröffentlichte Versionen), und fragt den
 * Host erst danach: zuerst nach der Berechtigung, nur mit ihr nach dem Datenkontext.
 */
public final class Vorschauerzeugung implements VorschauErzeugen {

    private final VorlagenRepository vorlagen;
    private final Berechtigungen berechtigungen;
    private final Platzhalterdatenquelle daten;
    private final Renderer renderer;
    private final Snapshotablage snapshots;
    private final Clock uhr;

    public Vorschauerzeugung(VorlagenRepository vorlagen, Berechtigungen berechtigungen, Platzhalterdatenquelle daten,
            Renderer renderer, Snapshotablage snapshots, Clock uhr) {
        this.vorlagen = vorlagen;
        this.berechtigungen = berechtigungen;
        this.daten = daten;
        this.renderer = renderer;
        this.snapshots = snapshots;
        this.uhr = uhr;
    }

    @Override
    public Vorschauergebnis erzeuge(BenutzerKennung benutzer, FachlicherBezug bezug, VorlagenId standardschreiben) {
        return vorlagen.standardschreiben(standardschreiben)
                .map(gefunden -> erzeuge(benutzer, bezug, gefunden))
                .orElseGet(Vorschauergebnis.StandardschreibenUnbekannt::new);
    }

    @Override
    public Vorschauergebnis aktualisiere(
            BenutzerKennung benutzer, FachlicherBezug bezug, VorlagenId standardschreiben, SnapshotId bisheriger) {
        Vorschauergebnis ergebnis = erzeuge(benutzer, bezug, standardschreiben);
        if (ergebnis instanceof Vorschauergebnis.Erzeugt) {
            snapshots.lade(bisheriger)
                    .filter(snapshot -> snapshot.benutzer().equals(benutzer))
                    .ifPresent(eigener -> snapshots.loesche(eigener.id()));
        }
        return ergebnis;
    }

    private Vorschauergebnis erzeuge(BenutzerKennung benutzer, FachlicherBezug bezug, Standardschreiben vorlage) {
        if (!vorlage.passtZu(bezug)) {
            return new Vorschauergebnis.BezugPasstNicht();
        }
        Optional<Vorlagenversion> inhalt = vorlage.neuesteVeroeffentlichteVersion();
        Optional<Vorlagenversion> briefpapier = vorlagen.briefpapier(vorlage.briefpapier())
                .flatMap(gefunden -> gefunden.neuesteVeroeffentlichteVersion());
        if (inhalt.isEmpty() || briefpapier.isEmpty()) {
            return new Vorschauergebnis.KeineVeroeffentlichteVersion();
        }
        if (!berechtigungen.darfErzeugen(benutzer, bezug, vorlage)) {
            return new Vorschauergebnis.NichtErlaubt();
        }
        var datenkontext = daten.liesDatenkontext(benutzer, bezug, vorlage);
        var snapshot = Snapshot.anlegen(SnapshotId.neu(), benutzer, bezug, briefpapier.get().verweis(),
                inhalt.get().verweis(), datenkontext, uhr.instant());
        String html = renderer.zusammenfuehren(briefpapier.get(), inhalt.get(), datenkontext);
        snapshots.speichere(snapshot);
        return new Vorschauergebnis.Erzeugt(snapshot.id(), html, snapshot.ablauf());
    }
}
