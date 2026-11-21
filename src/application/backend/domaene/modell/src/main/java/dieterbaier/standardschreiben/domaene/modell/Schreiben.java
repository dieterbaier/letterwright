package dieterbaier.standardschreiben.domaene.modell;

import java.time.Instant;

/**
 * Ein aus einem Standardschreiben erzeugtes und gespeichertes Dokument. Gespeichert werden das PDF, seine Prüfsumme und
 * die verwendeten Vorlagenversionen; ein Schreiben wird nie neu gerendert (ADR-007).
 */
public record Schreiben(
        SchreibenId id,
        FachlicherBezug bezug,
        Versionsverweis briefpapier,
        Versionsverweis standardschreiben,
        Dokumentverweis dokument,
        Pruefsumme pdfPruefsumme,
        Instant erstellt) {

    /**
     * Erzeugt das Schreiben aus dem Snapshot, aus dem sein PDF gerendert wurde. Bezug und Vorlagenversionen kommen aus
     * dem Snapshot, damit das Schreiben nicht von der Vorschau abweichen kann.
     *
     * @throws IllegalStateException wenn der Snapshot zum Zeitpunkt {@code jetzt} abgelaufen ist
     */
    public static Schreiben ausSnapshot(
            SchreibenId id, Snapshot snapshot, Dokumentverweis dokument, Pruefsumme pdfPruefsumme, Instant jetzt) {
        if (snapshot.istAbgelaufen(jetzt)) {
            throw new IllegalStateException("Snapshot " + snapshot.id().wert() + " ist abgelaufen");
        }
        return new Schreiben(id, snapshot.bezug(), snapshot.briefpapier(), snapshot.standardschreiben(), dokument,
                pdfPruefsumme, jetzt);
    }
}
