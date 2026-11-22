package dieterbaier.standardschreiben.domaene.modell.snapshot;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
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
 * <p>Aus einem Snapshot entsteht höchstens ein Schreiben: {@link #verbraucht()} geht nur von einem offenen aus und
 * gibt den Datenkontext frei (ADR-021).
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
    private final Snapshotzustand zustand;
    private final Instant ablauf;

    private Snapshot(SnapshotId id, BenutzerKennung benutzer, FachlicherBezug bezug, Versionsverweis briefpapier,
            Versionsverweis standardschreiben, Snapshotzustand zustand, Instant ablauf) {
        this.id = Pruefe.pflicht(id, "Die ID des Snapshots");
        this.benutzer = Pruefe.pflicht(benutzer, "Der Benutzer des Snapshots");
        this.bezug = Pruefe.pflicht(bezug, "Der fachliche Bezug des Snapshots");
        this.briefpapier = Pruefe.pflicht(briefpapier, "Die Briefpapier-Version des Snapshots");
        this.standardschreiben = Pruefe.pflicht(standardschreiben, "Die Standardschreiben-Version des Snapshots");
        this.zustand = Pruefe.pflicht(zustand, "Der Zustand des Snapshots");
        this.ablauf = Pruefe.pflicht(ablauf, "Der Ablaufzeitpunkt des Snapshots");
    }

    /** Legt einen Snapshot an, der {@link #GUELTIGKEIT} nach {@code jetzt} abläuft. */
    public static Snapshot anlegen(SnapshotId id, BenutzerKennung benutzer, FachlicherBezug bezug,
            Versionsverweis briefpapier, Versionsverweis standardschreiben, Datenkontext datenkontext, Instant jetzt) {
        return new Snapshot(id, benutzer, bezug, briefpapier, standardschreiben,
                new Snapshotzustand.Offen(datenkontext),
                Pruefe.pflicht(jetzt, "Der Zeitpunkt der Anlage").plus(GUELTIGKEIT));
    }

    /** Stellt einen gespeicherten Snapshot mit Zustand und Ablaufzeitpunkt wieder her. */
    public static Snapshot wiederherstellen(SnapshotId id, BenutzerKennung benutzer, FachlicherBezug bezug,
            Versionsverweis briefpapier, Versionsverweis standardschreiben, Snapshotzustand zustand, Instant ablauf) {
        return new Snapshot(id, benutzer, bezug, briefpapier, standardschreiben, zustand, ablauf);
    }

    /**
     * Der Snapshot, nachdem aus ihm ein Schreiben entstanden ist, ohne Datenkontext. Gedacht für
     * {@code Schreiben.ausSnapshot}.
     *
     * @throws IllegalStateException wenn aus dem Snapshot schon ein Schreiben entstanden ist
     */
    public Snapshot verbraucht() {
        if (zustand instanceof Snapshotzustand.Verbraucht) {
            throw new IllegalStateException("Aus Snapshot " + id.wert() + " ist schon ein Schreiben entstanden");
        }
        return new Snapshot(id, benutzer, bezug, briefpapier, standardschreiben, new Snapshotzustand.Verbraucht(),
                ablauf);
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

    public Snapshotzustand zustand() {
        return zustand;
    }

    public Instant ablauf() {
        return ablauf;
    }

    /** Ab dem Ablaufzeitpunkt ist der Snapshot abgelaufen. */
    public boolean istAbgelaufen(Instant jetzt) {
        return !jetzt.isBefore(ablauf);
    }

    /** Verwendbar nur für denselben Benutzer und Bezug und nur vor dem Ablauf. */
    public boolean istVerwendbarFuer(BenutzerKennung anfragender, FachlicherBezug angefragterBezug, Instant jetzt) {
        return benutzer.equals(anfragender)
                && bezug.equals(angefragterBezug)
                && !istAbgelaufen(jetzt);
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
