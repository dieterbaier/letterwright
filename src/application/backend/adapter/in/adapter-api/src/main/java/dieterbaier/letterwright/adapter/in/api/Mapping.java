package dieterbaier.letterwright.adapter.in.api;

import dieterbaier.letterwright.api.BusinessReference;
import dieterbaier.letterwright.api.SavedLetter;
import dieterbaier.letterwright.api.SaveResult;
import dieterbaier.letterwright.api.LetterTemplateVersion;
import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.Letter;

/**
 * Maps between the domain model and the types of the API (ADR-023). The types carry the same domain terms; the types of
 * the use cases are fully qualified here, those of the API are imported.
 */
final class Mapping {

    private Mapping() {
    }

    /**
     * The use case saves only from the user's own snapshot. Whoever receives a letter has therefore also created it, even
     * for {@code AlreadySaved}; the letter itself does not carry a user ID.
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
            // The use case behind the Java API is wired without a callback (ADR-023). If this case occurs anyway, the
            // wiring is wrong; the exception rolls back the host's transaction.
            case dieterbaier.letterwright.usecases.in.SaveResult.RejectedByHost rejected ->
                    throw new IllegalStateException("The Java API has no callback, but the host rejected"
                            + " the save: " + rejected.reason());
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
