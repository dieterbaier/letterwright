package dieterbaier.standardschreiben.starter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.spi.Aktion;
import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.FachlicherBezug;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class RollenBerechtigungspruefungTest {

    private static final FachlicherBezug KUNDE = new FachlicherBezug("Kunde", "4711");
    private static final Berechtigungsanfrage ERZEUGEN =
            new Berechtigungsanfrage("erika", Aktion.SCHREIBEN_ERZEUGEN, KUNDE);

    private final RollenBerechtigungspruefung pruefung =
            new RollenBerechtigungspruefung(new Berechtigungseigenschaften(Map.of()));

    @AfterEach
    void abmelden() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void mitDerRolleDerAktionIstSieErlaubt() {
        anmelden("erika", true, "ROLE_SSW_SCHREIBEN_ERZEUGEN");

        assertThat(pruefung.istErlaubt(ERZEUGEN)).isTrue();
    }

    @Test
    void mitDerRolleEinerAnderenAktionNicht() {
        anmelden("erika", true, "ROLE_SSW_SCHREIBEN_ANSEHEN");

        assertThat(pruefung.istErlaubt(ERZEUGEN)).isFalse();
    }

    @Test
    void ohneAnmeldungNicht() {
        assertThat(pruefung.istErlaubt(ERZEUGEN)).isFalse();
    }

    @Test
    void ohneAbgeschlosseneAnmeldungNicht() {
        anmelden("erika", false, "ROLE_SSW_SCHREIBEN_ERZEUGEN");

        assertThat(pruefung.istErlaubt(ERZEUGEN)).isFalse();
    }

    @Test
    void fuerEinenAnderenBenutzerNicht() {
        anmelden("max", true, "ROLE_SSW_SCHREIBEN_ERZEUGEN");

        assertThat(pruefung.istErlaubt(ERZEUGEN)).isFalse();
    }

    @Test
    void eineKonfigurierteRolleErsetztDenDefault() {
        var konfiguriert = new RollenBerechtigungspruefung(
                new Berechtigungseigenschaften(Map.of(Aktion.SCHREIBEN_ERZEUGEN, "ROLE_SACHBEARBEITUNG")));
        anmelden("erika", true, "ROLE_SSW_SCHREIBEN_ERZEUGEN");

        assertThat(konfiguriert.istErlaubt(ERZEUGEN)).isFalse();

        anmelden("erika", true, "ROLE_SACHBEARBEITUNG");

        assertThat(konfiguriert.istErlaubt(ERZEUGEN)).isTrue();
    }

    @Test
    void eineLeereRolleIstUnzulaessig() {
        assertThatThrownBy(() -> new Berechtigungseigenschaften(Map.of(Aktion.SCHREIBEN_LOESCHEN, " ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rollen.SCHREIBEN_LOESCHEN");
    }

    private static void anmelden(String name, boolean angemeldet, String rolle) {
        var anmeldung = new TestingAuthenticationToken(name, "geheim", rolle);
        anmeldung.setAuthenticated(angemeldet);
        SecurityContextHolder.getContext().setAuthentication(anmeldung);
    }
}
