package dieterbaier.standardschreiben.domaene.modell.vorlage;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VersionsnummerTest {

    @Test
    void versionsnummernBeginnenBeiEinsUndSindGeordnet() {
        assertThat(Versionsnummer.ERSTE.naechste()).isEqualTo(new Versionsnummer(2));
        assertThat(Versionsnummer.ERSTE).isLessThan(new Versionsnummer(2));
        lehntAb(() -> new Versionsnummer(0), "Versionsnummer");
        lehntAb(() -> new Versionsnummer(-1), "Versionsnummer");
    }
}
