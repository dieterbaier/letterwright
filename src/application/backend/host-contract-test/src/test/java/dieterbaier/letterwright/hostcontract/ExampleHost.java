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
 * Ein Host, der nur den Starter einbindet und alle drei SPI-Schnittstellen implementiert, dazu den optionalen Rückruf
 * nach dem Speichern. Dass diese Klasse kompiliert, ist der positive Teil des Nachweises.
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
                    && create.letterTemplate().name().equals("Mahnung");
        };
    }

    @Override
    public DeletionDecision decide(DeletionRequest request) {
        return DeletionDecision.logical();
    }

    /** Ein Host, der zu jedem Kunden höchstens ein Schreiben zulässt, etwa weil die Aufgabe damit erledigt ist. */
    @Override
    public CallbackResult created(SavedLetter letter) {
        return letter.reference().id().equals("4711")
                ? CallbackResult.rejected("Die Aufgabe zum Kunden 4711 ist bereits erledigt")
                : CallbackResult.accepted();
    }
}
