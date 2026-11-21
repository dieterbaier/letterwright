package dieterbaier.standardschreiben.domaene.modell;

import java.time.Duration;
import java.time.Instant;

/**
 * Befristet gespeicherter Datenkontext einer Vorschau, aus dem beim Speichern das PDF erzeugt wird (ADR-007).
 *
 * <p>Ein Snapshot ist an Benutzer, fachlichen Bezug und beide Vorlagenversionen gebunden. Für einen anderen Benutzer
 * oder Bezug und nach Ablauf ist er nicht verwendbar; der Anwendungsfall behandelt ihn dann wie einen nicht
 * vorhandenen.
 */
public record Snapshot(
        SnapshotId id,
        BenutzerKennung benutzer,
        FachlicherBezug bezug,
        Versionsverweis briefpapier,
        Versionsverweis standardschreiben,
        Datenkontext datenkontext,
        Instant ablauf) {

    /** So lange ist ein Snapshot nach seiner Anlage verwendbar (ADR-007). */
    public static final Duration GUELTIGKEIT = Duration.ofMinutes(30);

    /** Legt einen Snapshot an, der {@link #GUELTIGKEIT} nach {@code jetzt} abläuft. */
    public static Snapshot anlegen(
            SnapshotId id,
            BenutzerKennung benutzer,
            FachlicherBezug bezug,
            Versionsverweis briefpapier,
            Versionsverweis standardschreiben,
            Datenkontext datenkontext,
            Instant jetzt) {
        return new Snapshot(id, benutzer, bezug, briefpapier, standardschreiben, datenkontext,
                jetzt.plus(GUELTIGKEIT));
    }

    /** Ab dem Ablaufzeitpunkt ist der Snapshot abgelaufen. */
    public boolean istAbgelaufen(Instant jetzt) {
        return !jetzt.isBefore(ablauf);
    }

    /** Verwendbar nur für denselben Benutzer und Bezug und nur vor dem Ablauf. */
    public boolean istVerwendbarFuer(BenutzerKennung anfragender, FachlicherBezug angefragterBezug, Instant jetzt) {
        return benutzer.equals(anfragender) && bezug.equals(angefragterBezug) && !istAbgelaufen(jetzt);
    }

    /** Ob der neu gelesene Datenkontext dieselbe Prüfsumme hat wie der des Snapshots. */
    public boolean hatUnveraendertenDatenkontext(Datenkontext neuGelesen) {
        return datenkontext.pruefsumme().equals(neuGelesen.pruefsumme());
    }
}
