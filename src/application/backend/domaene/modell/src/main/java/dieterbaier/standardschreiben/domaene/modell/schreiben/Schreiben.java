package dieterbaier.standardschreiben.domaene.modell.schreiben;

import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.validierung.Pruefe;
import java.time.Instant;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Ein aus einem Standardschreiben erzeugtes und gespeichertes Dokument. Gespeichert werden das PDF, seine Prüfsumme und
 * die verwendeten Vorlagenversionen; ein Schreiben wird nie neu gerendert (ADR-007).
 *
 * <p>Jedes Schreiben kennt den Snapshot, aus dem es entstand. Zu einem Snapshot gibt es höchstens ein Schreiben, damit
 * ein wiederholtes Speichern derselben Vorschau kein zweites erzeugt (ADR-021).
 *
 * <p>Ob ein Schreiben noch sichtbar ist, sagt sein {@link Schreibenzustand}. Ein logisch gelöschtes behandeln alle
 * Anwendungsfälle wie ein nicht vorhandenes.
 */
@AggregateRoot
public final class Schreiben {

    @Identity
    private final SchreibenId id;
    private final SnapshotId snapshot;
    private final FachlicherBezug bezug;
    private final Versionsverweis briefpapier;
    private final Versionsverweis standardschreiben;
    private final Dokumentverweis dokument;
    private final Pruefsumme pdfPruefsumme;
    private final Instant erstellt;
    private final Schreibenzustand zustand;

    private Schreiben(SchreibenId id, SnapshotId snapshot, FachlicherBezug bezug, Versionsverweis briefpapier,
            Versionsverweis standardschreiben, Dokumentverweis dokument, Pruefsumme pdfPruefsumme, Instant erstellt,
            Schreibenzustand zustand) {
        this.id = Pruefe.pflicht(id, "Die ID des Schreibens");
        this.snapshot = Pruefe.pflicht(snapshot, "Der Snapshot des Schreibens");
        this.bezug = Pruefe.pflicht(bezug, "Der fachliche Bezug des Schreibens");
        this.briefpapier = Pruefe.pflicht(briefpapier, "Die Briefpapier-Version des Schreibens");
        this.standardschreiben = Pruefe.pflicht(standardschreiben, "Die Standardschreiben-Version des Schreibens");
        this.dokument = Pruefe.pflicht(dokument, "Der Dokumentverweis des Schreibens");
        this.pdfPruefsumme = Pruefe.pflicht(pdfPruefsumme, "Die Prüfsumme des PDFs");
        this.erstellt = Pruefe.pflicht(erstellt, "Der Erstellungszeitpunkt des Schreibens");
        this.zustand = Pruefe.pflicht(zustand, "Der Zustand des Schreibens");
    }

    /**
     * Erzeugt das Schreiben aus dem Snapshot, aus dem sein PDF gerendert wurde, und verbraucht ihn. Bezug und
     * Vorlagenversionen kommen aus dem Snapshot, damit das Schreiben nicht von der Vorschau abweichen kann. Aus einem
     * verbrauchten Snapshot entsteht kein weiteres Schreiben (ADR-021).
     *
     * <p>{@code jetzt} ist zugleich Prüfzeitpunkt für den Ablauf und Erstellungszeitpunkt. Der Anwendungsfall übergibt
     * den Zeitpunkt seiner Uhr, damit beides übereinstimmt und Tests die Grenze ohne Warten prüfen können.
     *
     * @throws IllegalStateException wenn der Snapshot zum Zeitpunkt {@code jetzt} abgelaufen oder schon verbraucht ist
     */
    public static SchreibenAusSnapshot ausSnapshot(
            SchreibenId id, Snapshot snapshot, Dokumentverweis dokument, Pruefsumme pdfPruefsumme, Instant jetzt) {
        Pruefe.pflicht(snapshot, "Der Snapshot");
        if (snapshot.istAbgelaufen(Pruefe.pflicht(jetzt, "Der Zeitpunkt"))) {
            throw new IllegalStateException("Snapshot " + snapshot.id().wert() + " ist abgelaufen");
        }
        Snapshot verbraucht = snapshot.verbraucht();
        return new SchreibenAusSnapshot(new Schreiben(id, snapshot.id(), snapshot.bezug(), snapshot.briefpapier(),
                snapshot.standardschreiben(), dokument, pdfPruefsumme, jetzt, new Schreibenzustand.Sichtbar()),
                verbraucht);
    }

    public static Schreiben wiederherstellen(SchreibenId id, SnapshotId snapshot, FachlicherBezug bezug,
            Versionsverweis briefpapier, Versionsverweis standardschreiben, Dokumentverweis dokument,
            Pruefsumme pdfPruefsumme, Instant erstellt, Schreibenzustand zustand) {
        return new Schreiben(id, snapshot, bezug, briefpapier, standardschreiben, dokument, pdfPruefsumme, erstellt,
                zustand);
    }

    /**
     * Das Schreiben, wie es nach einer logischen Löschung ist: Das PDF bleibt, sichtbar ist es nicht mehr. Ob gelöscht
     * werden darf und wie, entscheiden Berechtigung und Löschrichtlinie des Hosts, nicht das Schreiben.
     *
     * @throws IllegalStateException wenn das Schreiben schon logisch gelöscht ist
     */
    public Schreiben logischGeloescht(Instant jetzt) {
        if (zustand instanceof Schreibenzustand.LogischGeloescht) {
            throw new IllegalStateException("Schreiben " + id.wert() + " ist schon logisch gelöscht");
        }
        return new Schreiben(id, snapshot, bezug, briefpapier, standardschreiben, dokument, pdfPruefsumme, erstellt,
                new Schreibenzustand.LogischGeloescht(Pruefe.pflicht(jetzt, "Der Zeitpunkt")));
    }

    public boolean istSichtbar() {
        return zustand instanceof Schreibenzustand.Sichtbar;
    }

    public SchreibenId id() {
        return id;
    }

    public SnapshotId snapshot() {
        return snapshot;
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

    public Dokumentverweis dokument() {
        return dokument;
    }

    public Pruefsumme pdfPruefsumme() {
        return pdfPruefsumme;
    }

    public Instant erstellt() {
        return erstellt;
    }

    public Schreibenzustand zustand() {
        return zustand;
    }

    /** Gleich sind zwei Instanzen mit derselben ID. */
    @Override
    public boolean equals(Object anderes) {
        return anderes instanceof Schreiben schreiben && id.equals(schreiben.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Schreiben[id=" + id.wert() + ", erstellt=" + erstellt + "]";
    }
}
