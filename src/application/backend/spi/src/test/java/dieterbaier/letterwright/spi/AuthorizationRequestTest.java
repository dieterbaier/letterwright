package dieterbaier.letterwright.spi;

import static dieterbaier.letterwright.spi.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class AuthorizationRequestTest {

    private static final BusinessReference CUSTOMER = new BusinessReference("Kunde", "4711");
    private static final LetterTemplateId REMINDER =
            new LetterTemplateId(UUID.randomUUID(), "Mahnung");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void theUserIdRejectsMissingEmptyAndBlankValues(String value) {
        rejects(() -> AuthorizationRequest.forLetter(value, LetterAction.VIEW, CUSTOMER), "Die Benutzerkennung");
        rejects(() -> AuthorizationRequest.forCreation(value, CUSTOMER, REMINDER), "Die Benutzerkennung");
        rejects(() -> AuthorizationRequest.forTemplates(value, TemplateAction.EDIT), "Die Benutzerkennung");
    }

    @Test
    void theActionIsRequired() {
        rejects(() -> AuthorizationRequest.forLetter("erika", null, CUSTOMER), "Die Aktion");
        rejects(() -> AuthorizationRequest.forTemplates("erika", null), "Die Aktion");
    }

    @Test
    void aRequestForALetterNeedsAReference() {
        rejects(() -> AuthorizationRequest.forLetter("erika", LetterAction.VIEW, null), "Der fachliche Bezug");
        rejects(() -> AuthorizationRequest.forCreation("erika", null, REMINDER), "Der fachliche Bezug");
        rejects(() -> AuthorizationRequest.forCreation("erika", CUSTOMER, null), "Das Standardschreiben");
    }

    @Test
    void theRequestStatesByItsKindWhatItCarries() {
        var letter = (AuthorizationRequest.ForLetter) AuthorizationRequest.forLetter("erika",
                LetterAction.DELETE, CUSTOMER);
        AuthorizationRequest templates = AuthorizationRequest.forTemplates("erika", TemplateAction.PUBLISH);

        assertThat(letter).isEqualTo(new AuthorizationRequest.ForLetter("erika", LetterAction.DELETE, CUSTOMER));
        assertThat(letter.action()).isEqualTo(LetterAction.DELETE);
        assertThat(templates).isEqualTo(new AuthorizationRequest.ForTemplates("erika", TemplateAction.PUBLISH));
        assertThat(AuthorizationRequest.forCreation("erika", CUSTOMER, REMINDER))
                .isEqualTo(new AuthorizationRequest.ForCreation("erika", CUSTOMER, REMINDER));
    }
}
