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
 * Bridge to {@code features/built-in-authorization-check.feature}: one test per scenario, named after the
 * scenario title. The tests run through the auto-configuration and ask through the port, as the core does.
 */
class BuiltInAuthorizationCheckTest {

    private static final UserId ERIKA = new UserId("erika");
    private static final BusinessReference CUSTOMER_4711 = new BusinessReference("Customer", "4711");
    private static final LetterTemplate REMINDER =
            LetterTemplate.create(TemplateId.generate(), "Payment reminder", TemplateId.generate(), "Customer",
                    "<p>Payment reminder</p>");

    // Background: the host application provides no authorization check of its own.
    private final ApplicationContextRunner withoutCustomCheck = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LetterwrightAutoConfiguration.class))
            .withUserConfiguration(RequiredSpi.class);

    @AfterEach
    void logout() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void withTheRoleOfTheActionItIsAllowed() {
        // Given: Erika is logged in and has the role ROLE_LETTERWRIGHT_LETTERS_CREATE
        login("erika", "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        // When / Then: creating for customer 4711 is allowed
        withoutCustomCheck.run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class)))
                .isTrue());
    }

    @Test
    void withTheRoleOfAnotherActionItIsNotAllowed() {
        // Given: Erika is logged in and has only the role ROLE_LETTERWRIGHT_LETTERS_VIEW
        login("erika", "ROLE_LETTERWRIGHT_LETTERS_VIEW");

        // When / Then: creating is not allowed
        withoutCustomCheck.run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class)))
                .isFalse());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"nobody is logged in", "Erika accesses only anonymously"})
    void withoutAuthenticationNothingIsAllowed(String access) {
        // Given: the host has configured ROLE_ANONYMOUS for creating, and there is only the access from the example
        if (access.startsWith("Erika")) {
            SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key",
                    "erika", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        }

        // When / Then: creating is not allowed
        withoutCustomCheck
                .withPropertyValues("letterwright.authorization.roles.letters.CREATE=ROLE_ANONYMOUS")
                .run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class))).isFalse());
    }

    @Test
    void theHostDecidesWhatCountsAsAnonymousAccess() {
        // Given: the host classifies every login as anonymous with its own AuthenticationTrustResolver,
        // Erika is logged in and has the role ROLE_LETTERWRIGHT_LETTERS_CREATE
        login("erika", "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        // When / Then: creating is not allowed; without this resolver it would be allowed (first scenario)
        withoutCustomCheck.withUserConfiguration(EverythingIsAnonymous.class)
                .run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class))).isFalse());
    }

    @Test
    void nothingIsAllowedForAUserOtherThanTheLoggedInOne() {
        // Given: Max is logged in and has the role ROLE_LETTERWRIGHT_LETTERS_CREATE
        login("max", "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        // When / Then: the question for Erika is rejected
        withoutCustomCheck.run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class)))
                .isFalse());
    }

    @Test
    void aConfiguredRoleReplacesThePredefinedOne() {
        // Given: the host has configured ROLE_CASEWORK for creating,
        // Erika has only the predefined role
        login("erika", "ROLE_LETTERWRIGHT_LETTERS_CREATE");

        // When / Then: creating is not allowed
        withoutCustomCheck
                .withPropertyValues("letterwright.authorization.roles.letters.CREATE=ROLE_CASEWORK")
                .run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class))).isFalse());
    }

    @Test
    void aCustomAuthorizationCheckOfTheHostReplacesTheBuiltInOne() {
        // Given: the host provides a check of its own that allows everything; nobody is logged in

        // When / Then: creating is allowed because the custom check decides
        withoutCustomCheck.withUserConfiguration(AllowsEverything.class)
                .run(context -> assertThat(erikaMayCreate(context.getBean(Permissions.class))).isTrue());
    }

    private static boolean erikaMayCreate(Permissions permissions) {
        return permissions.mayCreate(ERIKA, CUSTOMER_4711, REMINDER);
    }

    private static void login(String name, String role) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(name, "secret", role));
    }

    @Configuration(proxyBeanMethods = false)
    static class RequiredSpi {

        @Bean
        PlaceholderDataProvider placeholderDataProvider() {
            return request -> new DataContext(Map.of());
        }

        @Bean
        DeletionPolicy deletionPolicy() {
            return request -> DeletionDecision.blocked("Retention period");
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
