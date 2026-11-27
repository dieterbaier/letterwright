package dieterbaier.letterwright.adapter.out.host;

import static dieterbaier.letterwright.adapter.out.host.TestData.ERIKA;
import static dieterbaier.letterwright.adapter.out.host.TestData.CUSTOMER;
import static dieterbaier.letterwright.adapter.out.host.TestData.REMINDER;
import static dieterbaier.letterwright.adapter.out.host.TestData.LETTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.TemplateAction;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.letter.DeletionDecision;
import dieterbaier.letterwright.domain.model.letter.CallbackResult;
import dieterbaier.letterwright.spi.DataRequest;
import dieterbaier.letterwright.spi.DeletionRequest;
import dieterbaier.letterwright.validation.ValidationException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.FieldSource;

class MappingTest {

    @Test
    void theBusinessReferenceMapsLosslesslyInBothDirections() {
        dieterbaier.letterwright.spi.BusinessReference inSpi = Mapping.toSpi(CUSTOMER);

        assertThat(inSpi).isEqualTo(new dieterbaier.letterwright.spi.BusinessReference("Kunde", "4711"));
        assertThat(Mapping.toDomain(inSpi)).isEqualTo(CUSTOMER);
    }

    @ParameterizedTest
    @EnumSource(TemplateAction.class)
    void everyTemplateActionMapsToTheSameNamedOne(TemplateAction action) {
        assertThat(Mapping.toSpi(action).name()).isEqualTo(action.name());
    }

    @ParameterizedTest
    @EnumSource(LetterAction.class)
    void everyLetterActionMapsToTheSameNamedOne(LetterAction action) {
        assertThat(Mapping.toSpi(action).name()).isEqualTo(action.name());
    }

    static final List<DeletionDecision> DELETION_DECISIONS = List.of(DeletionDecision.hard(),
            DeletionDecision.logical(), DeletionDecision.blocked("Aufbewahrungsfrist bis 2036"));

    @ParameterizedTest
    @FieldSource("DELETION_DECISIONS")
    void everyDeletionDecisionMapsLosslesslyInBothDirections(DeletionDecision decision) {
        dieterbaier.letterwright.spi.DeletionDecision inSpi = Mapping.toSpi(decision);

        assertThat(inSpi.getClass().getSimpleName()).isEqualTo(decision.getClass().getSimpleName());
        assertThat(Mapping.toDomain(inSpi)).isEqualTo(decision);
    }

    @Test
    void everyCallbackResultOfTheHostMapsToTheSameNamedOne() {
        assertThat(Mapping.toDomain(dieterbaier.letterwright.spi.CallbackResult.accepted()))
                .isEqualTo(CallbackResult.accepted());
        assertThat(Mapping.toDomain(dieterbaier.letterwright.spi.CallbackResult.rejected("Erledigt")))
                .isEqualTo(CallbackResult.rejected("Erledigt"));
    }

    @Test
    void theReasonForTheBlockIsKept() {
        assertThat(Mapping.toSpi(DeletionDecision.blocked("Frist")))
                .isEqualTo(dieterbaier.letterwright.spi.DeletionDecision.blocked("Frist"));
    }

    @Test
    void bothSidesKnowTheSameValues() {
        assertThat(names(dieterbaier.letterwright.spi.TemplateAction.values()))
                .isEqualTo(names(TemplateAction.values()));
        assertThat(names(dieterbaier.letterwright.spi.LetterAction.values()))
                .isEqualTo(names(LetterAction.values()));
        assertThat(kinds(dieterbaier.letterwright.spi.DeletionDecision.class))
                .isEqualTo(kinds(DeletionDecision.class));
    }

    @Test
    void theDataContextMapsLosslesslyInBothDirections() {
        Map<String, Object> values = Map.of("name", "Erika", "items", List.of(Map.of("betrag", "12,00 €")));
        DataContext inTheDomain = DataContext.from(values);

        dieterbaier.letterwright.spi.DataContext inSpi = Mapping.toSpi(inTheDomain);

        assertThat(inSpi.values()).isEqualTo(values);
        assertThat(Mapping.toDomain(inSpi)).isEqualTo(inTheDomain);
    }

    @Test
    void anInvalidDataContextOfTheHostIsAContractViolation() {
        var invalid = new dieterbaier.letterwright.spi.DataContext(Map.of("betrag", 12));

        assertThatThrownBy(() -> Mapping.toDomain(invalid))
                .isInstanceOf(HostContractViolated.class)
                .hasMessageContaining("PlaceholderDataProvider")
                .hasCauseInstanceOf(ValidationException.class);
    }

    @Test
    void theDataRequestCarriesUserReferenceAndLetterTemplate() {
        DataRequest request = Mapping.dataRequest(ERIKA, CUSTOMER, REMINDER);

        assertThat(request.userId()).isEqualTo("erika");
        assertThat(request.reference()).isEqualTo(Mapping.toSpi(CUSTOMER));
        assertThat(request.letterTemplate().id()).isEqualTo(REMINDER.id().value());
        assertThat(request.letterTemplate().name()).isEqualTo("Mahnung");
    }

    @Test
    void theDeletionRequestCarriesLetterReferenceAndCreationTime() {
        DeletionRequest request = Mapping.deletionRequest(ERIKA, LETTER);

        assertThat(request.userId()).isEqualTo("erika");
        assertThat(request.letter()).isEqualTo(LETTER.id().value());
        assertThat(request.reference()).isEqualTo(Mapping.toSpi(CUSTOMER));
        assertThat(request.created()).isEqualTo(LETTER.created());
    }

    private static List<String> kinds(Class<?> sealedType) {
        return Arrays.stream(sealedType.getPermittedSubclasses()).map(Class::getSimpleName).toList();
    }

    private static List<String> names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }
}
