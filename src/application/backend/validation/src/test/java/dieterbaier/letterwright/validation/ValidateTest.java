package dieterbaier.letterwright.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidateTest {

    @Test
    void requiredReturnsThePresentValue() {
        assertThat(Validate.required("x", "Wert")).isEqualTo("x");
    }

    @Test
    void requiredReportsAMissingValueWithLabel() {
        assertThatThrownBy(() -> Validate.required(null, "Die Kennung"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Die Kennung fehlt");
    }

    @Test
    void notEmptyAllowsWhitespace() {
        assertThat(Validate.notEmpty(" ", "Text")).isEqualTo(" ");
    }

    @Test
    void notEmptyRejectsEmptyAndMissingTexts() {
        assertThatThrownBy(() -> Validate.notEmpty("", "Der Text"))
                .isInstanceOf(ValidationException.class).hasMessageContaining("Der Text darf nicht leer sein");
        assertThatThrownBy(() -> Validate.notEmpty(null, "Der Text"))
                .isInstanceOf(ValidationException.class).hasMessage("Der Text fehlt");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\n"})
    void notBlankRejectsEmptyAndWhitespace(String text) {
        assertThatThrownBy(() -> Validate.notBlank(text, "Der Name"))
                .isInstanceOf(ValidationException.class).hasMessageContaining("Der Name");
    }

    @Test
    void notBlankReturnsTheTextAndReportsMissingTexts() {
        assertThat(Validate.notBlank(" a ", "Name")).isEqualTo(" a ");
        assertThatThrownBy(() -> Validate.notBlank(null, "Der Name"))
                .isInstanceOf(ValidationException.class).hasMessage("Der Name fehlt");
    }

    @Test
    void atLeastChecksTheLowerBoundInclusively() {
        assertThat(Validate.atLeast(1, 1, "Nummer")).isEqualTo(1);
        assertThatThrownBy(() -> Validate.atLeast(0, 1, "Die Nummer"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Die Nummer muss mindestens 1 sein, war 0");
    }

    @Test
    void requiredWithoutGapsNamesThePositionOfTheMissingElement() {
        assertThat(Validate.requiredWithoutGaps(List.of("a"), "Liste")).containsExactly("a");
        assertThatThrownBy(() -> Validate.requiredWithoutGaps(Arrays.asList("a", null), "Die Versionen"))
                .isInstanceOf(ValidationException.class).hasMessage("Die Versionen[1] fehlt");
        assertThatThrownBy(() -> Validate.requiredWithoutGaps(null, "Die Versionen"))
                .isInstanceOf(ValidationException.class).hasMessage("Die Versionen fehlt");
    }

    @Test
    void validationExceptionIsAnIllegalArgumentExceptionAndKeepsTheCause() {
        Exception cause = new IllegalStateException("innen");

        assertThat(new ValidationException("außen", cause))
                .isInstanceOf(IllegalArgumentException.class)
                .hasCause(cause);
    }
}
