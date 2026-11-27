package dieterbaier.letterwright.starter;

import dieterbaier.letterwright.spi.AuthorizationRequest;
import dieterbaier.letterwright.spi.TemplateAction;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Rollen der Default-{@link dieterbaier.letterwright.spi.AuthorizationCheck}. Ohne Eintrag gilt die Authority
 * {@code ROLE_LETTERWRIGHT_TEMPLATES_<AKTION>} oder {@code ROLE_LETTERWRIGHT_LETTERS_<AKTION>}, etwa {@code ROLE_LETTERWRIGHT_LETTERS_CREATE};
 * ein Host vergibt sie in Spring Security mit {@code roles("SSW_SCHREIBEN_ERZEUGEN")}. Abweichend:
 *
 * <pre>letterwright.authorization.roles.letters.CREATE=ROLE_SACHBEARBEITUNG</pre>
 *
 * @param templates die Authority je Aktion an Vorlagen, wo sie vom Default abweicht
 * @param letters die Authority je Rolle an Schreiben, wo sie vom Default abweicht
 */
@ConfigurationProperties("letterwright.authorization.roles")
record AuthorizationProperties(
        @DefaultValue Map<TemplateAction, String> templates, @DefaultValue Map<LetterRole, String> letters) {

    private static final String PREFIX = "letterwright.authorization.roles.";

    AuthorizationProperties {
        templates = checked(templates, PREFIX + "templates.");
        letters = checked(letters, PREFIX + "letters.");
    }

    /** Die Authority, die ein Benutzer für die Anfrage braucht. */
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
                throw new IllegalArgumentException(key + action.name() + " darf nicht leer sein");
            }
        });
        return roles.isEmpty() ? Map.of() : Map.copyOf(new EnumMap<>(roles));
    }
}
