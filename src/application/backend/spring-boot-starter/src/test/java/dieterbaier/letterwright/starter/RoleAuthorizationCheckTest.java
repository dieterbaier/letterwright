package dieterbaier.letterwright.starter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.spi.LetterAction;
import dieterbaier.letterwright.spi.TemplateAction;
import dieterbaier.letterwright.spi.AuthorizationRequest;
import dieterbaier.letterwright.spi.BusinessReference;
import dieterbaier.letterwright.spi.LetterTemplateId;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class RoleAuthorizationCheckTest {

    private static final BusinessReference CUSTOMER = new BusinessReference("Kunde", "4711");
    private static final AuthorizationRequest CREATE =
            AuthorizationRequest.forCreation("erika", CUSTOMER, new LetterTemplateId(UUID.randomUUID(), "Mahnung"));

    private final RoleAuthorizationCheck check = new RoleAuthorizationCheck(
            new AuthorizationProperties(Map.of(), Map.of()), new AuthenticationTrustResolverImpl());

    @AfterEach
    void logout() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void withTheRoleOfTheActionItIsAllowed() {
        login("erika", true, "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        assertThat(check.isAllowed(CREATE)).isTrue();
    }

    @Test
    void anActionOnTemplatesHasItsOwnRole() {
        login("erika", true, "ROLE_LETTERWRIGHT_TEMPLATES_PUBLISH");

        assertThat(check.isAllowed(AuthorizationRequest.forTemplates("erika", TemplateAction.PUBLISH)))
                .isTrue();
        assertThat(check.isAllowed(AuthorizationRequest.forTemplates("erika", TemplateAction.EDIT)))
                .isFalse();
    }

    @Test
    void anActionOnSavedLettersHasItsOwnRole() {
        login("erika", true, "ROLE_LETTERWRIGHT_LETTERS_DOWNLOAD");

        assertThat(check.isAllowed(AuthorizationRequest.forLetter("erika", LetterAction.DOWNLOAD, CUSTOMER)))
                .isTrue();
        assertThat(check.isAllowed(AuthorizationRequest.forLetter("erika", LetterAction.VIEW, CUSTOMER)))
                .isFalse();
    }

    @Test
    void notWithTheRoleOfAnotherAction() {
        login("erika", true, "ROLE_LETTERWRIGHT_LETTERS_VIEW");

        assertThat(check.isAllowed(CREATE)).isFalse();
    }

    @Test
    void notWithoutAuthentication() {
        assertThat(check.isAllowed(CREATE)).isFalse();
    }

    @Test
    void notWithoutCompletedAuthentication() {
        login("erika", false, "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        assertThat(check.isAllowed(CREATE)).isFalse();
    }

    @Test
    void notForAnotherUser() {
        login("max", true, "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        assertThat(check.isAllowed(CREATE)).isFalse();
    }

    @Test
    void aConfiguredRoleReplacesTheDefault() {
        var configured = new RoleAuthorizationCheck(
                new AuthorizationProperties(Map.of(), Map.of(LetterRole.CREATE, "ROLE_SACHBEARBEITUNG")),
                new AuthenticationTrustResolverImpl());
        login("erika", true, "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        assertThat(configured.isAllowed(CREATE)).isFalse();

        login("erika", true, "ROLE_SACHBEARBEITUNG");

        assertThat(configured.isAllowed(CREATE)).isTrue();
    }

    @Test
    void anEmptyRoleIsNotAllowed() {
        assertThatThrownBy(() -> new AuthorizationProperties(Map.of(), Map.of(LetterRole.DELETE, " ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("roles.letters.DELETE");
        assertThatThrownBy(() -> new AuthorizationProperties(Map.of(TemplateAction.EDIT, ""), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("roles.templates.EDIT");
    }

    private static void login(String name, boolean loggedIn, String role) {
        var authentication = new TestingAuthenticationToken(name, "geheim", role);
        authentication.setAuthenticated(loggedIn);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
