package dieterbaier.letterwright.adapter.out.host;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.CallbackResult;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.spi.LetterSaveCallback;
import dieterbaier.letterwright.usecases.out.SaveCallback;
import org.jspecify.annotations.Nullable;

/**
 * Ruft den optionalen Rückruf {@link LetterSaveCallback} des Hosts (ADR-023). Der Host liegt außerhalb der
 * Vertrauensgrenze: Hält er den Null-Vertrag des SPI nicht ein, meldet der Adapter das als {@link HostContractViolated}.
 * Eine Ausnahme des Hosts reicht er unverändert durch; sie ist ein technischer Fehler.
 */
public final class HostSaveCallback implements SaveCallback {

    private final LetterSaveCallback callback;

    private HostSaveCallback(LetterSaveCallback callback) {
        this.callback = callback;
    }

    /** Für einen Host, der den Rückruf bereitstellt. */
    public static SaveCallback forReference(LetterSaveCallback callback) {
        return new HostSaveCallback(callback);
    }

    /** Für einen Host ohne Rückruf und für die Java-API: Jedes Schreiben ist angenommen, gefragt wird niemand. */
    public static SaveCallback none() {
        return (user, letter) -> CallbackResult.accepted();
    }

    @Override
    public CallbackResult created(UserId user, Letter letter) {
        dieterbaier.letterwright.spi.@Nullable CallbackResult answer =
                callback.created(Mapping.savedLetter(user, letter));
        if (answer == null) {
            throw new HostContractViolated("Der Rückruf NachDemSpeichern des Hosts hat null statt eines Ergebnisses"
                    + " geliefert");
        }
        return Mapping.toDomain(answer);
    }
}
