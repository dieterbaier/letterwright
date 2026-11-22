package dieterbaier.standardschreiben.starter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.spi.SchreibenAktion;
import dieterbaier.standardschreiben.spi.VorlagenAktion;
import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.FachlicherBezug;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class RollenBerechtigungspruefungTest {

    private static final FachlicherBezug KUNDE = new FachlicherBezug("Kunde", "4711");
    private static final Berechtigungsanfrage ERZEUGEN =
            Berechtigungsanfrage.zuSchreiben("erika", SchreibenAktion.ERZEUGEN, KUNDE);

    private final RollenBerechtigungspruefung pruefung = new RollenBerechtigungspruefung(
            new Berechtigungseigenschaften(Map.of(), Map.of()), new AuthenticationTrustResolverImpl());

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
    void eineAktionAnVorlagenHatIhreEigeneRolle() {
        anmelden("erika", true, "ROLE_SSW_VORLAGEN_VEROEFFENTLICHEN");

        assertThat(pruefung.istErlaubt(Berechtigungsanfrage.zuVorlagen("erika", VorlagenAktion.VEROEFFENTLICHEN)))
                .isTrue();
        assertThat(pruefung.istErlaubt(Berechtigungsanfrage.zuVorlagen("erika", VorlagenAktion.BEARBEITEN)))
                .isFalse();
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
                new Berechtigungseigenschaften(Map.of(), Map.of(SchreibenAktion.ERZEUGEN, "ROLE_SACHBEARBEITUNG")),
                new AuthenticationTrustResolverImpl());
        anmelden("erika", true, "ROLE_SSW_SCHREIBEN_ERZEUGEN");

        assertThat(konfiguriert.istErlaubt(ERZEUGEN)).isFalse();

        anmelden("erika", true, "ROLE_SACHBEARBEITUNG");

        assertThat(konfiguriert.istErlaubt(ERZEUGEN)).isTrue();
    }

    @Test
    void eineLeereRolleIstUnzulaessig() {
        assertThatThrownBy(() -> new Berechtigungseigenschaften(Map.of(), Map.of(SchreibenAktion.LOESCHEN, " ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rollen.schreiben.LOESCHEN");
        assertThatThrownBy(() -> new Berechtigungseigenschaften(Map.of(VorlagenAktion.BEARBEITEN, ""), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rollen.vorlagen.BEARBEITEN");
    }

    private static void anmelden(String name, boolean angemeldet, String rolle) {
        var anmeldung = new TestingAuthenticationToken(name, "geheim", rolle);
        anmeldung.setAuthenticated(angemeldet);
        SecurityContextHolder.getContext().setAuthentication(anmeldung);
    }
}
