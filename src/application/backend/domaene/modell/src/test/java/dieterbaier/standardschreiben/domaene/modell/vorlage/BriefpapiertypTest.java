package dieterbaier.standardschreiben.domaene.modell.vorlage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BriefpapiertypTest {

    @Test
    void briefpapierOhneTypIstGeschaeftlichFormB() {
        assertThat(Briefpapiertyp.STANDARD).isEqualTo(Briefpapiertyp.GESCHAEFTLICH_FORM_B);
    }
}
