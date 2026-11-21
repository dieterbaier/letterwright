package dieterbaier.standardschreiben.validierung;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PruefeTest {

    @Test
    void pflichtLiefertDenVorhandenenWert() {
        assertThat(Pruefe.pflicht("x", "Wert")).isEqualTo("x");
    }

    @Test
    void pflichtMeldetEinenFehlendenWertMitBezeichnung() {
        assertThatThrownBy(() -> Pruefe.pflicht(null, "Die Kennung"))
                .isInstanceOf(Validierungsfehler.class)
                .hasMessage("Die Kennung fehlt");
    }

    @Test
    void nichtLeerLaesstLeerraumZu() {
        assertThat(Pruefe.nichtLeer(" ", "Text")).isEqualTo(" ");
    }

    @Test
    void nichtLeerLehntLeereUndFehlendeTexteAb() {
        assertThatThrownBy(() -> Pruefe.nichtLeer("", "Der Text"))
                .isInstanceOf(Validierungsfehler.class).hasMessageContaining("Der Text darf nicht leer sein");
        assertThatThrownBy(() -> Pruefe.nichtLeer(null, "Der Text"))
                .isInstanceOf(Validierungsfehler.class).hasMessage("Der Text fehlt");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\n"})
    void nichtBlankLehntLeereUndLeerraumAb(String text) {
        assertThatThrownBy(() -> Pruefe.nichtBlank(text, "Der Name"))
                .isInstanceOf(Validierungsfehler.class).hasMessageContaining("Der Name");
    }

    @Test
    void nichtBlankLiefertDenTextUndMeldetFehlendeTexte() {
        assertThat(Pruefe.nichtBlank(" a ", "Name")).isEqualTo(" a ");
        assertThatThrownBy(() -> Pruefe.nichtBlank(null, "Der Name"))
                .isInstanceOf(Validierungsfehler.class).hasMessage("Der Name fehlt");
    }

    @Test
    void mindestensPrueftDieUntergrenzeEinschliesslich() {
        assertThat(Pruefe.mindestens(1, 1, "Nummer")).isEqualTo(1);
        assertThatThrownBy(() -> Pruefe.mindestens(0, 1, "Die Nummer"))
                .isInstanceOf(Validierungsfehler.class)
                .hasMessage("Die Nummer muss mindestens 1 sein, war 0");
    }

    @Test
    void pflichtOhneLueckenNenntDiePositionDesFehlendenElements() {
        assertThat(Pruefe.pflichtOhneLuecken(List.of("a"), "Liste")).containsExactly("a");
        assertThatThrownBy(() -> Pruefe.pflichtOhneLuecken(Arrays.asList("a", null), "Die Versionen"))
                .isInstanceOf(Validierungsfehler.class).hasMessage("Die Versionen[1] fehlt");
        assertThatThrownBy(() -> Pruefe.pflichtOhneLuecken(null, "Die Versionen"))
                .isInstanceOf(Validierungsfehler.class).hasMessage("Die Versionen fehlt");
    }

    @Test
    void validierungsfehlerIstEineIllegalArgumentExceptionUndBehaeltDieUrsache() {
        Exception ursache = new IllegalStateException("innen");

        assertThat(new Validierungsfehler("außen", ursache))
                .isInstanceOf(IllegalArgumentException.class)
                .hasCause(ursache);
    }
}
