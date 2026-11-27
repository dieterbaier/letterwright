package dieterbaier.letterwright.adapter.out.host;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.CallbackResult;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.spi.LetterSaveCallback;
import dieterbaier.letterwright.usecases.out.SaveCallback;
import org.jspecify.annotations.Nullable;

/**
 * Calls the host's optional callback {@link LetterSaveCallback} (ADR-023). The host lies outside the trust boundary: if
 * it does not keep the SPI's null contract, the adapter reports this as {@link HostContractViolated}. It passes an
 * exception of the host through unchanged; it is a technical error.
 */
public final class HostSaveCallback implements SaveCallback {

    private final LetterSaveCallback callback;

    private HostSaveCallback(LetterSaveCallback callback) {
        this.callback = callback;
    }

    /** For a host that provides the callback. */
    public static SaveCallback forReference(LetterSaveCallback callback) {
        return new HostSaveCallback(callback);
    }

    /** For a host without a callback and for the Java API: every letter is accepted, nobody is asked. */
    public static SaveCallback none() {
        return (user, letter) -> CallbackResult.accepted();
    }

    @Override
    public CallbackResult created(UserId user, Letter letter) {
        dieterbaier.letterwright.spi.@Nullable CallbackResult answer =
                callback.created(Mapping.savedLetter(user, letter));
        if (answer == null) {
            throw new HostContractViolated("The LetterSaveCallback of the host returned null instead of a"
                    + " CallbackResult");
        }
        return Mapping.toDomain(answer);
    }
}
