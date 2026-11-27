package dieterbaier.letterwright.domain.model.template;

import java.io.Serial;

/**
 * Eine Vorlage soll etwas tun, das der Zustand ihrer Versionen nicht erlaubt, etwa eine veröffentlichte Version ändern.
 * Anders als ein {@link dieterbaier.letterwright.validation.ValidationException} ist das kein ungültiger Wert,
 * sondern ein fachlich verbotener Schritt.
 */
public final class IllegalStateTransition extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    IllegalStateTransition(TemplateId template, String message) {
        super("Vorlage " + template.value() + ": " + message);
    }
}
