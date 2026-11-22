package dieterbaier.standardschreiben.domaene.modell.vorlage;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VersionsverweisTest {

    @Test
    void versionsverweisVerlangtVorlageUndNummer() {
        lehntAb(() -> new Versionsverweis(null, Versionsnummer.ERSTE), "Vorlage des Versionsverweises");
        lehntAb(() -> new Versionsverweis(VorlagenId.neu(), null), "Nummer des Versionsverweises");
    }

    @Test
    void versionsverweiseSindWertgleich() {
        VorlagenId vorlage = VorlagenId.neu();

        assertThat(new Versionsverweis(vorlage, Versionsnummer.ERSTE))
                .isEqualTo(new Versionsverweis(vorlage, new Versionsnummer(1)));
    }
}
