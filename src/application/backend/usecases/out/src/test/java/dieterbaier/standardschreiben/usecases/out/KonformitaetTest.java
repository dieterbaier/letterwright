package dieterbaier.standardschreiben.usecases.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class KonformitaetTest {

    @Test
    void einNichtKonformesPdfHatSeineBefunde() {
        var befunde = new ArrayList<>(List.of("PDF/UA-1: Bild ohne Alternativtext"));
        var nichtKonform = (Konformitaet.NichtKonform) Konformitaet.nichtKonform(befunde);
        befunde.clear();

        assertThat(nichtKonform.befunde()).containsExactly("PDF/UA-1: Bild ohne Alternativtext");
        assertThat(Konformitaet.konform()).isInstanceOf(Konformitaet.Konform.class);
    }

    @Test
    void einNichtKonformesPdfBrauchtMindestensEinenBefund() {
        assertThatThrownBy(() -> Konformitaet.nichtKonform(List.of())).hasMessageContaining("mindestens einen Befund");
        assertThatThrownBy(() -> Konformitaet.nichtKonform(null)).hasMessageContaining("Befunde");
    }
}
