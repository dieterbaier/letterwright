package dieterbaier.letterwright.domain.model.template;

import java.io.Serial;

/**
 * A template is asked to do something the state of its versions does not allow, such as changing a published version.
 * Unlike a {@link dieterbaier.letterwright.validation.ValidationException} this is not an invalid value but a step the
 * domain forbids.
 */
public final class IllegalStateTransition extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    IllegalStateTransition(TemplateId template, String message) {
        super("Template " + template.value() + ": " + message);
    }
}
