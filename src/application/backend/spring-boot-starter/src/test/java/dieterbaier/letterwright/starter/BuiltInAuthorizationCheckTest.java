package dieterbaier.letterwright.starter;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import dieterbaier.letterwright.spi.AuthorizationCheck;
import dieterbaier.letterwright.spi.DataContext;
import dieterbaier.letterwright.spi.DeletionDecision;
import dieterbaier.letterwright.spi.DeletionPolicy;
import dieterbaier.letterwright.spi.PlaceholderDataProvider;
import dieterbaier.letterwright.usecases.out.Permissions;
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
class BuiltInAuthorizationCheckTest {

    private static final UserId ERIKA = new UserId("erika");
    private static final BusinessReference CUSTOMER_4711 = new BusinessReference("Kunde", "4711");
    private static final LetterTemplate REMINDER =
            LetterTemplate.create(TemplateId.generate(), "Mahnung", TemplateId.generate(), "Kunde", "<p>Mahnung</p>");

    // Grundlage: Die Host-Anwendung stellt keine eigene Berechtigungsprüfung bereit.
    private final ApplicationContextRunner withoutCustomCheck = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LetterwrightAutoConfiguration.class))
            .withUserConfiguration(RequiredSpi.class);

    @AfterEach
    void logout() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void withTheRoleOfTheActionItIsAllowed() {
        // Given: Erika ist angemeldet und hat die Rolle ROLE_LETTERWRIGHT_LETTERS_CREATE
        login("erika", "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        // When / Then: Erzeugen zum Kunden 4711 ist erlaubt
        withoutCustomCheck.run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class)))
                .isTrue());
    }

    @Test
    void withTheRoleOfAnotherActionItIsNotAllowed() {
        // Given: Erika ist angemeldet und hat nur die Rolle ROLE_LETTERWRIGHT_LETTERS_VIEW
        login("erika", "ROLE_LETTERWRIGHT_LETTERS_VIEW");

        // When / Then: Erzeugen ist nicht erlaubt
        withoutCustomCheck.run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class)))
                .isFalse());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"niemand ist angemeldet", "Erika greift nur anonym zu"})
    void withoutAuthenticationNothingIsAllowed(String access) {
        // Given: Der Host hat ROLE_ANONYMOUS fürs Erzeugen konfiguriert, und es gibt nur den Zugriff aus dem Beispiel
        if (access.startsWith("Erika")) {
            SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key",
                    "erika", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        }

        // When / Then: Erzeugen ist nicht erlaubt
        withoutCustomCheck
                .withPropertyValues("letterwright.authorization.roles.letters.CREATE=ROLE_ANONYMOUS")
                .run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class))).isFalse());
    }

    @Test
    void theHostDecidesWhatCountsAsAnonymousAccess() {
        // Given: Der Host stuft mit einem eigenen AuthenticationTrustResolver jede Anmeldung als anonym ein,
        // Erika ist angemeldet und hat die Rolle ROLE_LETTERWRIGHT_LETTERS_CREATE
        login("erika", "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        // When / Then: Erzeugen ist nicht erlaubt; ohne diesen Resolver wäre es erlaubt (erstes Szenario)
        withoutCustomCheck.withUserConfiguration(EverythingIsAnonymous.class)
                .run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class))).isFalse());
    }

    @Test
    void nothingIsAllowedForAUserOtherThanTheLoggedInOne() {
        // Given: Max ist angemeldet und hat die Rolle ROLE_LETTERWRIGHT_LETTERS_CREATE
        login("max", "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        // When / Then: die Frage für Erika wird abgelehnt
        withoutCustomCheck.run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class)))
                .isFalse());
    }

    @Test
    void aConfiguredRoleReplacesThePredefinedOne() {
        // Given: Der Host hat ROLE_SACHBEARBEITUNG fürs Erzeugen konfiguriert,
        // Erika hat nur die vorgegebene Rolle
        login("erika", "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        // When / Then: Erzeugen ist nicht erlaubt
        withoutCustomCheck
                .withPropertyValues("letterwright.authorization.roles.letters.CREATE=ROLE_SACHBEARBEITUNG")
                .run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class))).isFalse());
    }

    @Test
    void aCustomAuthorizationCheckOfTheHostReplacesTheBuiltInOne() {
        // Given: Der Host stellt eine eigene Prüfung bereit, die alles erlaubt; niemand ist angemeldet

        // When / Then: Erzeugen ist erlaubt, weil die eigene Prüfung entscheidet
        withoutCustomCheck.withUserConfiguration(AllowsEverything.class)
                .run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class))).isTrue());
    }

    private static boolean erikaMayCreate(Permissions permissions) {
        return permissions.mayCreate(ERIKA, CUSTOMER_4711, REMINDER);
    }

    private static void login(String name, String role) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(name, "geheim", role));
    }

    @Configuration(proxyBeanMethods = false)
    static class RequiredSpi {

        @Bean
        PlaceholderDataProvider placeholderDataProvider() {
            return request -> new DataContext(Map.of());
        }

        @Bean
        DeletionPolicy deletionPolicy() {
            return request -> DeletionDecision.blocked("Aufbewahrungsfrist");
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class EverythingIsAnonymous {

        @Bean
        AuthenticationTrustResolver everythingIsAnonymous() {
            return new AuthenticationTrustResolverImpl() {
                @Override
                public boolean isAnonymous(@Nullable Authentication authentication) {
                    return true;
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class AllowsEverything {

        @Bean
        AuthorizationCheck customAuthorizationCheck() {
            return request -> true;
        }
    }
}
