package dieterbaier.standardschreiben.domaene.modell;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SeitenanfrageTest {

    @Test
    void dieAnfrageKenntIhrenVersatz() {
        assertThat(new Seitenanfrage(0, 20).versatz()).isZero();
        assertThat(new Seitenanfrage(3, 20).versatz()).isEqualTo(60);
        assertThat(new Seitenanfrage(Integer.MAX_VALUE, Integer.MAX_VALUE).versatz())
                .as("ohne Überlauf").isPositive();
    }

    @Test
    void dieAnfrageBeginntBeiNullUndHatMindestensEinenEintrag() {
        lehntAb(() -> new Seitenanfrage(-1, 20), "Seitennummer");
        lehntAb(() -> new Seitenanfrage(0, 0), "Seitengröße");
    }
}
