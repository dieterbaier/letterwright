package dieterbaier.standardschreiben.starter;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.spi.Berechtigungspruefung;
import dieterbaier.standardschreiben.spi.Datenkontext;
import dieterbaier.standardschreiben.spi.Loeschentscheidung;
import dieterbaier.standardschreiben.spi.Loeschrichtlinie;
import dieterbaier.standardschreiben.spi.PlatzhalterdatenAnbieter;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Brücke zu {@code features/eingebaute-berechtigungspruefung.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die Tests laufen durch die Autokonfiguration und fragen über den Port, wie es der Kern tut.
 */
class EingebauteBerechtigungspruefungTest {

    private static final BenutzerKennung ERIKA = new BenutzerKennung("erika");
    private static final FachlicherBezug KUNDE_4711 = new FachlicherBezug("Kunde", "4711");

    // Grundlage: Die Host-Anwendung stellt keine eigene Berechtigungsprüfung bereit.
    private final ApplicationContextRunner ohneEigenePruefung = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(StandardschreibenAutoKonfiguration.class))
            .withUserConfiguration(PflichtSpi.class);

    @AfterEach
    void abmelden() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void mitDerRolleDerAktionIstSieErlaubt() {
        // Given: Erika ist angemeldet und hat die Rolle ROLE_SSW_SCHREIBEN_ERZEUGEN
        anmelden("erika", "ROLE_SSW_SCHREIBEN_ERZEUGEN");

        // When / Then: Erzeugen zum Kunden 4711 ist erlaubt
        ohneEigenePruefung.run(kontext -> assertThat(darfErikaErzeugen(kontext.getBean(Berechtigungen.class)))
                .isTrue());
    }

    @Test
    void mitDerRolleEinerAnderenAktionIstSieNichtErlaubt() {
        // Given: Erika ist angemeldet und hat nur die Rolle ROLE_SSW_SCHREIBEN_ANSEHEN
        anmelden("erika", "ROLE_SSW_SCHREIBEN_ANSEHEN");

        // When / Then: Erzeugen ist nicht erlaubt
        ohneEigenePruefung.run(kontext -> assertThat(darfErikaErzeugen(kontext.getBean(Berechtigungen.class)))
                .isFalse());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"niemand ist angemeldet", "Erika greift nur anonym zu"})
    void ohneAnmeldungIstNichtsErlaubt(String zugriff) {
        // Given: Der Host hat ROLE_ANONYMOUS fürs Erzeugen konfiguriert, und es gibt nur den Zugriff aus dem Beispiel
        if (zugriff.startsWith("Erika")) {
            SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("schluessel",
                    "erika", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        }

        // When / Then: Erzeugen ist nicht erlaubt
        ohneEigenePruefung
                .withPropertyValues("standardschreiben.berechtigung.rollen.schreiben.ERZEUGEN=ROLE_ANONYMOUS")
                .run(kontext -> assertThat(darfErikaErzeugen(kontext.getBean(Berechtigungen.class))).isFalse());
    }

    @Test
    void wasAlsAnonymerZugriffGiltBestimmtDerHost() {
        // Given: Der Host stuft mit einem eigenen AuthenticationTrustResolver jede Anmeldung als anonym ein,
        // Erika ist angemeldet und hat die Rolle ROLE_SSW_SCHREIBEN_ERZEUGEN
        anmelden("erika", "ROLE_SSW_SCHREIBEN_ERZEUGEN");

        // When / Then: Erzeugen ist nicht erlaubt; ohne diesen Resolver wäre es erlaubt (erstes Szenario)
        ohneEigenePruefung.withUserConfiguration(AllesIstAnonym.class)
                .run(kontext -> assertThat(darfErikaErzeugen(kontext.getBean(Berechtigungen.class))).isFalse());
    }

    @Test
    void fuerEinenAnderenAlsDenAngemeldetenBenutzerIstNichtsErlaubt() {
        // Given: Max ist angemeldet und hat die Rolle ROLE_SSW_SCHREIBEN_ERZEUGEN
        anmelden("max", "ROLE_SSW_SCHREIBEN_ERZEUGEN");

        // When / Then: die Frage für Erika wird abgelehnt
        ohneEigenePruefung.run(kontext -> assertThat(darfErikaErzeugen(kontext.getBean(Berechtigungen.class)))
                .isFalse());
    }

    @Test
    void eineKonfigurierteRolleErsetztDieVorgegebene() {
        // Given: Der Host hat ROLE_SACHBEARBEITUNG fürs Erzeugen konfiguriert,
        // Erika hat nur die vorgegebene Rolle
        anmelden("erika", "ROLE_SSW_SCHREIBEN_ERZEUGEN");

        // When / Then: Erzeugen ist nicht erlaubt
        ohneEigenePruefung
                .withPropertyValues("standardschreiben.berechtigung.rollen.schreiben.ERZEUGEN=ROLE_SACHBEARBEITUNG")
                .run(kontext -> assertThat(darfErikaErzeugen(kontext.getBean(Berechtigungen.class))).isFalse());
    }

    @Test
    void eineEigeneBerechtigungspruefungDesHostsErsetztDieEingebaute() {
        // Given: Der Host stellt eine eigene Prüfung bereit, die alles erlaubt; niemand ist angemeldet

        // When / Then: Erzeugen ist erlaubt, weil die eigene Prüfung entscheidet
        ohneEigenePruefung.withUserConfiguration(ErlaubtAlles.class)
                .run(kontext -> assertThat(darfErikaErzeugen(kontext.getBean(Berechtigungen.class))).isTrue());
    }

    private static boolean darfErikaErzeugen(Berechtigungen berechtigungen) {
        return berechtigungen.darf(ERIKA, SchreibenAktion.ERZEUGEN, KUNDE_4711);
    }

    private static void anmelden(String name, String rolle) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(name, "geheim", rolle));
    }

    @Configuration(proxyBeanMethods = false)
    static class PflichtSpi {

        @Bean
        PlatzhalterdatenAnbieter platzhalterdatenAnbieter() {
            return anfrage -> new Datenkontext(Map.of());
        }

        @Bean
        Loeschrichtlinie loeschrichtlinie() {
            return anfrage -> Loeschentscheidung.gesperrt("Aufbewahrungsfrist");
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class AllesIstAnonym {

        @Bean
        AuthenticationTrustResolver allesIstAnonym() {
            return new AuthenticationTrustResolverImpl() {
                @Override
                public boolean isAnonymous(@Nullable Authentication anmeldung) {
                    return true;
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ErlaubtAlles {

        @Bean
        Berechtigungspruefung eigeneBerechtigungspruefung() {
            return anfrage -> true;
        }
    }
}
