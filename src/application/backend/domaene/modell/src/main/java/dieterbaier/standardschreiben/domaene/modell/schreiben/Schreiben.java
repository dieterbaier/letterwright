package dieterbaier.standardschreiben.domaene.modell.schreiben;

import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.validierung.Pruefe;
import java.time.Instant;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Ein aus einem Standardschreiben erzeugtes und gespeichertes Dokument. Gespeichert werden das PDF, seine Prüfsumme und
 * die verwendeten Vorlagenversionen; ein Schreiben wird nie neu gerendert (ADR-007).
 */
@AggregateRoot
public final class Schreiben {

    @Identity
    private final SchreibenId id;
    private final FachlicherBezug bezug;
    private final Versionsverweis briefpapier;
    private final Versionsverweis standardschreiben;
    private final Dokumentverweis dokument;
    private final Pruefsumme pdfPruefsumme;
    private final Instant erstellt;

    private Schreiben(SchreibenId id, FachlicherBezug bezug, Versionsverweis briefpapier,
            Versionsverweis standardschreiben, Dokumentverweis dokument, Pruefsumme pdfPruefsumme, Instant erstellt) {
        this.id = Pruefe.pflicht(id, "Die ID des Schreibens");
        this.bezug = Pruefe.pflicht(bezug, "Der fachliche Bezug des Schreibens");
        this.briefpapier = Pruefe.pflicht(briefpapier, "Die Briefpapier-Version des Schreibens");
        this.standardschreiben = Pruefe.pflicht(standardschreiben, "Die Standardschreiben-Version des Schreibens");
        this.dokument = Pruefe.pflicht(dokument, "Der Dokumentverweis des Schreibens");
        this.pdfPruefsumme = Pruefe.pflicht(pdfPruefsumme, "Die Prüfsumme des PDFs");
        this.erstellt = Pruefe.pflicht(erstellt, "Der Erstellungszeitpunkt des Schreibens");
    }

    /**
     * Erzeugt das Schreiben aus dem Snapshot, aus dem sein PDF gerendert wurde. Bezug und Vorlagenversionen kommen aus
     * dem Snapshot, damit das Schreiben nicht von der Vorschau abweichen kann.
     *
     * <p>{@code jetzt} ist zugleich Prüfzeitpunkt für den Ablauf und Erstellungszeitpunkt. Der Anwendungsfall übergibt
     * den Zeitpunkt seiner Uhr, damit beides übereinstimmt und Tests die Grenze ohne Warten prüfen können.
     *
     * @throws IllegalStateException wenn der Snapshot zum Zeitpunkt {@code jetzt} abgelaufen ist
     */
    public static Schreiben ausSnapshot(
            SchreibenId id, Snapshot snapshot, Dokumentverweis dokument, Pruefsumme pdfPruefsumme, Instant jetzt) {
        Pruefe.pflicht(snapshot, "Der Snapshot");
        if (snapshot.istAbgelaufen(Pruefe.pflicht(jetzt, "Der Zeitpunkt"))) {
            throw new IllegalStateException("Snapshot " + snapshot.id().wert() + " ist abgelaufen");
        }
        return new Schreiben(id, snapshot.bezug(), snapshot.briefpapier(), snapshot.standardschreiben(), dokument,
                pdfPruefsumme, jetzt);
    }

    public static Schreiben wiederherstellen(SchreibenId id, FachlicherBezug bezug, Versionsverweis briefpapier,
            Versionsverweis standardschreiben, Dokumentverweis dokument, Pruefsumme pdfPruefsumme, Instant erstellt) {
        return new Schreiben(id, bezug, briefpapier, standardschreiben, dokument, pdfPruefsumme, erstellt);
    }

    public SchreibenId id() {
        return id;
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
