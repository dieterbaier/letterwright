package dieterbaier.letterwright.hostcontract;

import dieterbaier.letterwright.spi.AuthorizationRequest;
import dieterbaier.letterwright.spi.AuthorizationCheck;
import dieterbaier.letterwright.spi.DataRequest;
import dieterbaier.letterwright.spi.DataContext;
import dieterbaier.letterwright.spi.SavedLetter;
import dieterbaier.letterwright.spi.DeletionRequest;
import dieterbaier.letterwright.spi.DeletionDecision;
import dieterbaier.letterwright.spi.DeletionPolicy;
import dieterbaier.letterwright.spi.LetterSaveCallback;
import dieterbaier.letterwright.spi.PlaceholderDataProvider;
import dieterbaier.letterwright.spi.CallbackResult;
import java.util.Map;

/**
 * A host that includes only the starter and implements all three SPI interfaces, plus the optional letter save
 * callback. That this class compiles is the positive part of the proof.
 */
final class ExampleHost implements PlaceholderDataProvider, AuthorizationCheck, DeletionPolicy, LetterSaveCallback {

    @Override
    public DataContext dataContext(DataRequest request) {
        return new DataContext(Map.of("customer", request.reference().id()));
    }

    @Override
    public boolean isAllowed(AuthorizationRequest request) {
        return switch (request) {
            case AuthorizationRequest.ForTemplates templates -> false;
            case AuthorizationRequest.ForLetter letter -> letter.userId().equals("erika");
            case AuthorizationRequest.ForCreation create -> create.userId().equals("erika")
                    && create.letterTemplate().name().equals("Payment reminder");
        };
    }

    @Override
    public DeletionDecision decide(DeletionRequest request) {
        return DeletionDecision.logical();
    }

    /** A host that allows at most one letter per customer, for example because that completes the task. */
    @Override
    public CallbackResult created(SavedLetter letter) {
        return letter.reference().id().equals("4711")
                ? CallbackResult.rejected("The task for customer 4711 is already done")
                : CallbackResult.accepted();
    }
}
