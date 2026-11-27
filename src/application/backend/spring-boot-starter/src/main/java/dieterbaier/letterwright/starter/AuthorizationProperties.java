package dieterbaier.letterwright.starter;

import dieterbaier.letterwright.spi.AuthorizationRequest;
import dieterbaier.letterwright.spi.TemplateAction;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Roles of the default {@link dieterbaier.letterwright.spi.AuthorizationCheck}. Without an entry the authority
 * {@code ROLE_LETTERWRIGHT_TEMPLATES_<ACTION>} or {@code ROLE_LETTERWRIGHT_LETTERS_<ACTION>} applies, for example
 * {@code ROLE_LETTERWRIGHT_LETTERS_CREATE}; a host grants it in Spring Security with
 * {@code roles("LETTERWRIGHT_LETTERS_CREATE")}. To deviate, set {@code templates.<ACTION>} or {@code letters.<ACTION>}:
 *
 * <pre>letterwright.authorization.roles.letters.CREATE=ROLE_CASEWORK</pre>
 *
 * @param templates the authority per action on templates, where it deviates from the default
 * @param letters the authority per action on letters, where it deviates from the default
 */
@ConfigurationProperties("letterwright.authorization.roles")
record AuthorizationProperties(
        @DefaultValue Map<TemplateAction, String> templates, @DefaultValue Map<LetterRole, String> letters) {

    private static final String PREFIX = "letterwright.authorization.roles.";

    AuthorizationProperties {
        templates = checked(templates, PREFIX + "templates.");
        letters = checked(letters, PREFIX + "letters.");
    }

    /** The authority a user needs for the request. */
    String role(AuthorizationRequest request) {
        return switch (request) {
            case AuthorizationRequest.ForTemplates forTemplates -> templates.getOrDefault(
                    forTemplates.action(), "ROLE_LETTERWRIGHT_TEMPLATES_" + forTemplates.action().name());
            case AuthorizationRequest.ForLetter forLetter -> role(switch (forLetter.action()) {
                case VIEW -> LetterRole.VIEW;
                case DOWNLOAD -> LetterRole.DOWNLOAD;
                case DELETE -> LetterRole.DELETE;
            });
            case AuthorizationRequest.ForCreation forCreation -> role(LetterRole.CREATE);
        };
    }

    private String role(LetterRole role) {
        return letters.getOrDefault(role, "ROLE_LETTERWRIGHT_LETTERS_" + role.name());
    }

    private static <A extends Enum<A>> Map<A, String> checked(Map<A, String> roles, String key) {
        roles.forEach((action, role) -> {
            if (role.isBlank()) {
                throw new IllegalArgumentException(key + action.name() + " must not be blank");
            }
        });
        return roles.isEmpty() ? Map.of() : Map.copyOf(new EnumMap<>(roles));
    }
}
