package dieterbaier.letterwright.adapter.in.api;

import dieterbaier.letterwright.api.BusinessReference;
import dieterbaier.letterwright.api.SavedLetter;
import dieterbaier.letterwright.api.SaveResult;
import dieterbaier.letterwright.api.LetterTemplateVersion;
import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.Letter;

/**
 * Bildet zwischen dem Domänenmodell und den Typen der API ab (ADR-023). Die Typen tragen dieselben Fachbegriffe; die
 * Typen der Anwendungsfälle sind hier voll qualifiziert, die der API importiert.
 */
final class Mapping {

    private Mapping() {
    }

    /**
     * Der Anwendungsfall speichert nur aus einem eigenen Snapshot des Benutzers. Wer ein Schreiben erhält, hat es deshalb
     * auch erzeugt, auch bei {@code AlreadySaved}; das Schreiben selbst führt keine Benutzerkennung.
     */
    static SaveResult toApi(
            dieterbaier.letterwright.usecases.in.SaveResult result, UserId user) {
        return switch (result) {
            case dieterbaier.letterwright.usecases.in.SaveResult.Saved saved ->
                    new SaveResult.Saved(toApi(saved.letter(), user));
            case dieterbaier.letterwright.usecases.in.SaveResult.AlreadySaved already ->
                    new SaveResult.AlreadySaved(toApi(already.letter(), user));
            case dieterbaier.letterwright.usecases.in.SaveResult.SnapshotUnknown unknown ->
                    new SaveResult.SnapshotUnknown();
            case dieterbaier.letterwright.usecases.in.SaveResult.SnapshotExpired expired ->
                    new SaveResult.SnapshotExpired();
            case dieterbaier.letterwright.usecases.in.SaveResult.NotAllowed notAllowed ->
                    new SaveResult.NotAllowed();
            case dieterbaier.letterwright.usecases.in.SaveResult.DataContextChanged changed ->
                    new SaveResult.DataContextChanged();
            case dieterbaier.letterwright.usecases.in.SaveResult.NotConformant notConformant ->
                    new SaveResult.NotConformant(notConformant.findings());
            // Der Anwendungsfall hinter der Java-API ist ohne Rückruf verdrahtet (ADR-023). Kommt der Fall trotzdem,
            // ist die Verdrahtung falsch; die Ausnahme rollt die Transaktion des Hosts zurück.
            case dieterbaier.letterwright.usecases.in.SaveResult.RejectedByHost rejected ->
                    throw new IllegalStateException("Über die Java-API gibt es keinen Rückruf, der Host hat aber"
                            + " abgelehnt: " + rejected.reason());
        };
    }

    private static SavedLetter toApi(Letter letter, UserId user) {
        return new SavedLetter(letter.id().value(),
                new BusinessReference(letter.reference().type(), letter.reference().id()),
                new LetterTemplateVersion(letter.letterTemplate().template().value(),
                        letter.letterTemplate().number().value()),
                user.value(), letter.created());
    }
}
