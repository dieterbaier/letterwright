package dieterbaier.standardschreiben.domaene.modell;

import dieterbaier.standardschreiben.validierung.Pruefe;
import java.time.Duration;
import java.time.Instant;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Befristet gespeicherter Datenkontext einer Vorschau, aus dem beim Speichern das PDF erzeugt wird (ADR-007).
 *
 * <p>Ein Snapshot ist an Benutzer, fachlichen Bezug und beide Vorlagenversionen gebunden. Für einen anderen Benutzer
 * oder Bezug und nach Ablauf ist er nicht verwendbar; der Anwendungsfall behandelt ihn dann wie einen nicht
 * vorhandenen. Die Vorlagenversionen kennt er nur als {@link Versionsverweis}, sie gehören zu ihren Vorlagen.
 *
 * <p>Den Zeitpunkt bekommen die Methoden als Parameter. Der Anwendungsfall liest seine Uhr einmal und prüft und erzeugt
 * mit demselben Zeitpunkt; das Modell selbst liest keine Uhr.
 */
@AggregateRoot
public final class Snapshot {

    /** So lange ist ein Snapshot nach seiner Anlage verwendbar (ADR-007). */
    public static final Duration GUELTIGKEIT = Duration.ofMinutes(30);

    @Identity
    private final SnapshotId id;
    private final BenutzerKennung benutzer;
    private final FachlicherBezug bezug;
    private final Versionsverweis briefpapier;
    private final Versionsverweis standardschreiben;
    private final Datenkontext datenkontext;
    private final Instant ablauf;

    private Snapshot(SnapshotId id, BenutzerKennung benutzer, FachlicherBezug bezug, Versionsverweis briefpapier,
            Versionsverweis standardschreiben, Datenkontext datenkontext, Instant ablauf) {
        this.id = Pruefe.pflicht(id, "Die ID des Snapshots");
        this.benutzer = Pruefe.pflicht(benutzer, "Der Benutzer des Snapshots");
        this.bezug = Pruefe.pflicht(bezug, "Der fachliche Bezug des Snapshots");
        this.briefpapier = Pruefe.pflicht(briefpapier, "Die Briefpapier-Version des Snapshots");
        this.standardschreiben = Pruefe.pflicht(standardschreiben, "Die Standardschreiben-Version des Snapshots");
        this.datenkontext = Pruefe.pflicht(datenkontext, "Der Datenkontext des Snapshots");
        this.ablauf = Pruefe.pflicht(ablauf, "Der Ablaufzeitpunkt des Snapshots");
    }

    /** Legt einen Snapshot an, der {@link #GUELTIGKEIT} nach {@code jetzt} abläuft. */
    public static Snapshot anlegen(SnapshotId id, BenutzerKennung benutzer, FachlicherBezug bezug,
            Versionsverweis briefpapier, Versionsverweis standardschreiben, Datenkontext datenkontext, Instant jetzt) {
        return new Snapshot(id, benutzer, bezug, briefpapier, standardschreiben, datenkontext,
                Pruefe.pflicht(jetzt, "Der Zeitpunkt der Anlage").plus(GUELTIGKEIT));
    }

    /** Stellt einen gespeicherten Snapshot mit seinem Ablaufzeitpunkt wieder her. */
    public static Snapshot wiederherstellen(SnapshotId id, BenutzerKennung benutzer, FachlicherBezug bezug,
            Versionsverweis briefpapier, Versionsverweis standardschreiben, Datenkontext datenkontext, Instant ablauf) {
        return new Snapshot(id, benutzer, bezug, briefpapier, standardschreiben, datenkontext, ablauf);
    }

    public SnapshotId id() {
        return id;
    }

    public BenutzerKennung benutzer() {
        return benutzer;
    }

    public FachlicherBezug bezug() {
        return bezug;
    }

    public Versionsverweis briefpapier() {
        return briefpapier;
    }

    public Versionsverweis standardschreiben() {
        return standardschreiben;
    }

    public Datenkontext datenkontext() {
        return datenkontext;
    }

    public Instant ablauf() {
        return ablauf;
    }

    /** Ab dem Ablaufzeitpunkt ist der Snapshot abgelaufen. */
    public boolean istAbgelaufen(Instant jetzt) {
        return !Pruefe.pflicht(jetzt, "Der Zeitpunkt").isBefore(ablauf);
    }

    /** Verwendbar nur für denselben Benutzer und Bezug und nur vor dem Ablauf. */
    public boolean istVerwendbarFuer(BenutzerKennung anfragender, FachlicherBezug angefragterBezug, Instant jetzt) {
        return benutzer.equals(Pruefe.pflicht(anfragender, "Der anfragende Benutzer"))
                && bezug.equals(Pruefe.pflicht(angefragterBezug, "Der angefragte Bezug"))
                && !istAbgelaufen(jetzt);
    }

    /** Ob der neu gelesene Datenkontext dieselbe Prüfsumme hat wie der des Snapshots. */
    public boolean hatUnveraendertenDatenkontext(Datenkontext neuGelesen) {
        return datenkontext.pruefsumme().equals(Pruefe.pflicht(neuGelesen, "Der neu gelesene Datenkontext").pruefsumme());
    }

    /** Gleich sind zwei Instanzen mit derselben ID. */
    @Override
    public boolean equals(Object anderes) {
        return anderes instanceof Snapshot snapshot && id.equals(snapshot.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        // Ohne Datenkontext: Er kann personenbezogen sein.
        return "Snapshot[id=" + id.wert() + ", ablauf=" + ablauf + "]";
    }
}
