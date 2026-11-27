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
        assertThat(Validate.required("x", "Value")).isEqualTo("x");
    }

    @Test
    void requiredReportsAMissingValueWithLabel() {
        assertThatThrownBy(() -> Validate.required(null, "The ID"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("The ID is missing");
    }

    @Test
    void notEmptyAllowsWhitespace() {
        assertThat(Validate.notEmpty(" ", "Text")).isEqualTo(" ");
    }

    @Test
    void notEmptyRejectsEmptyAndMissingTexts() {
        assertThatThrownBy(() -> Validate.notEmpty("", "The text"))
                .isInstanceOf(ValidationException.class).hasMessageContaining("The text must not be empty");
        assertThatThrownBy(() -> Validate.notEmpty(null, "The text"))
                .isInstanceOf(ValidationException.class).hasMessage("The text is missing");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\n"})
    void notBlankRejectsEmptyAndWhitespace(String text) {
        assertThatThrownBy(() -> Validate.notBlank(text, "The name"))
                .isInstanceOf(ValidationException.class).hasMessageContaining("The name");
    }

    @Test
    void notBlankReturnsTheTextAndReportsMissingTexts() {
        assertThat(Validate.notBlank(" a ", "Name")).isEqualTo(" a ");
        assertThatThrownBy(() -> Validate.notBlank(null, "The name"))
                .isInstanceOf(ValidationException.class).hasMessage("The name is missing");
    }

    @Test
    void atLeastChecksTheLowerBoundInclusively() {
        assertThat(Validate.atLeast(1, 1, "Number")).isEqualTo(1);
        assertThatThrownBy(() -> Validate.atLeast(0, 1, "The number"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("The number must be at least 1, was 0");
    }

    @Test
    void requiredWithoutGapsNamesThePositionOfTheMissingElement() {
        assertThat(Validate.requiredWithoutGaps(List.of("a"), "List")).containsExactly("a");
        assertThatThrownBy(() -> Validate.requiredWithoutGaps(Arrays.asList("a", null), "Versions"))
                .isInstanceOf(ValidationException.class).hasMessage("Versions[1] is missing");
        assertThatThrownBy(() -> Validate.requiredWithoutGaps(null, "Versions"))
                .isInstanceOf(ValidationException.class).hasMessage("Versions is missing");
    }

    @Test
    void validationExceptionIsAnIllegalArgumentExceptionAndKeepsTheCause() {
        Exception cause = new IllegalStateException("inner");

        assertThat(new ValidationException("outer", cause))
                .isInstanceOf(IllegalArgumentException.class)
                .hasCause(cause);
    }
}
