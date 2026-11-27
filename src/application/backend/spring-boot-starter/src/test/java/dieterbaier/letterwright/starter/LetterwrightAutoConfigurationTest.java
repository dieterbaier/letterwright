package dieterbaier.letterwright.starter;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.letter.DocumentReference;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.domain.model.letter.LetterState;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.template.VersionNumber;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import dieterbaier.letterwright.spi.AuthorizationRequest;
import dieterbaier.letterwright.spi.AuthorizationCheck;
import dieterbaier.letterwright.spi.DataContext;
import dieterbaier.letterwright.spi.DeletionDecision;
import dieterbaier.letterwright.spi.DeletionPolicy;
import dieterbaier.letterwright.spi.LetterSaveCallback;
import dieterbaier.letterwright.spi.CallbackResult;
import dieterbaier.letterwright.spi.PlaceholderDataProvider;
import dieterbaier.letterwright.spi.LetterAction;
import dieterbaier.letterwright.spi.LetterTemplateId;
import dieterbaier.letterwright.spi.TemplateAction;
import dieterbaier.letterwright.usecases.out.Permissions;
import dieterbaier.letterwright.usecases.out.DeletionRules;
import dieterbaier.letterwright.usecases.out.PlaceholderDataSource;
import dieterbaier.letterwright.usecases.out.SaveCallback;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** Zugleich Brücke zum Szenario „Ohne Rückruf des Hosts nimmt die Bibliothek jedes neue Schreiben an“ in rueckruf-nach-dem-speichern.feature. */
class LetterwrightAutoConfigurationTest {

    private static final dieterbaier.letterwright.spi.BusinessReference CUSTOMER =
            new dieterbaier.letterwright.spi.BusinessReference("Kunde", "4711");
    private static final LetterTemplateId REMINDER = new LetterTemplateId(UUID.randomUUID(), "Mahnung");

    private static final UserId ERIKA = new UserId("erika");
    private static final Letter LETTER = Letter.restore(LetterId.generate(), SnapshotId.generate(),
            new BusinessReference("Kunde", "4711"), new VersionReference(TemplateId.generate(), VersionNumber.FIRST),
            new VersionReference(TemplateId.generate(), VersionNumber.FIRST), new DocumentReference("store/1"),
            Checksum.of(new byte[] {1}), Instant.parse("2026-11-25T10:00:00Z"), new LetterState.Visible());

    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LetterwrightAutoConfiguration.class));

    @Test
    void withoutCustomAuthorizationCheckTheDefaultImplementationApplies() {
        context.withUserConfiguration(RequiredSpi.class).run(running -> {
            assertThat(running).hasSingleBean(AuthorizationCheck.class);
            assertThat(running.getBean(AuthorizationCheck.class)).isInstanceOf(RoleAuthorizationCheck.class);
            assertThat(running).hasSingleBean(Permissions.class);
            assertThat(running).hasSingleBean(PlaceholderDataSource.class);
            assertThat(running).hasSingleBean(DeletionRules.class);
        });
    }

    @Test
    void aCustomAuthorizationCheckOfTheHostOverridesTheDefaultImplementation() {
        context.withUserConfiguration(RequiredSpi.class, CustomAuthorization.class).run(running -> {
            assertThat(running).hasSingleBean(AuthorizationCheck.class);
            assertThat(running).doesNotHaveBean(RoleAuthorizationCheck.class);
            assertThat(running.getBean(Permissions.class)
                    .may(new UserId("erika"), dieterbaier.letterwright.domain.model.LetterAction
                            .DELETE, new BusinessReference("Kunde", "4711")))
                    .isTrue();
        });
    }

    @Test
    void withoutPlaceholderDataProviderTheContextDoesNotStart() {
        context.withUserConfiguration(OnlyDeletionPolicy.class)
                .run(running -> assertThat(running).hasFailed()
                        .getFailure().hasMessageContaining("PlaceholderDataProvider"));
    }

    @Test
    void withoutDeletionPolicyTheContextDoesNotStart() {
        context.withUserConfiguration(OnlyPlaceholderData.class)
                .run(running -> assertThat(running).hasFailed()
                        .getFailure().hasMessageContaining("DeletionPolicy"));
    }

    @Test
    void theRolesAreConfigurable() {
        context.withUserConfiguration(RequiredSpi.class)
                .withPropertyValues("letterwright.authorization.roles.letters.CREATE=ROLE_SACHBEARBEITUNG",
                        "letterwright.authorization.roles.templates.EDIT=ROLE_REDAKTION")
                .run(running -> {
                    AuthorizationProperties properties = running.getBean(AuthorizationProperties.class);
                    assertThat(properties.role(AuthorizationRequest.forCreation("erika", CUSTOMER, REMINDER))).isEqualTo("ROLE_SACHBEARBEITUNG");
                    assertThat(properties.role(AuthorizationRequest.forLetter("erika", LetterAction.DELETE, CUSTOMER))).isEqualTo("ROLE_LETTERWRIGHT_LETTERS_DELETE");
                    assertThat(properties.role(AuthorizationRequest.forTemplates("erika", TemplateAction.EDIT))).isEqualTo("ROLE_REDAKTION");
                    assertThat(properties.role(AuthorizationRequest.forTemplates("erika", TemplateAction.PUBLISH)))
                            .isEqualTo("ROLE_LETTERWRIGHT_TEMPLATES_PUBLISH");
                });
    }

    @Test
    void withoutACallbackOfTheHostTheLibraryAcceptsEveryNewLetter() {
        context.withUserConfiguration(RequiredSpi.class).run(running -> assertThat(
                running.getBean(SaveCallback.class).created(ERIKA, LETTER))
                .isEqualTo(dieterbaier.letterwright.domain.model.letter.CallbackResult.accepted()));
    }

    @Test
    void aCallbackOfTheHostIsAsked() {
        context.withUserConfiguration(RequiredSpi.class, RejectingCallback.class).run(running -> assertThat(
                running.getBean(SaveCallback.class).created(ERIKA, LETTER))
                .isEqualTo(dieterbaier.letterwright.domain.model.letter.CallbackResult.rejected(
                        "Aufgabe ist erledigt")));
    }

    @Test
    void withSeveralCallbacksThePrimaryOneIsAsked() {
        context.withUserConfiguration(RequiredSpi.class, RejectingCallback.class, PrimaryCallback.class)
                .run(running -> assertThat(running.getBean(SaveCallback.class).created(ERIKA, LETTER))
                        .isEqualTo(dieterbaier.letterwright.domain.model.letter.CallbackResult.accepted()));
    }

    @Test
    void severalCallbacksWithoutPrimaryPreventTheContextFromStarting() {
        context.withUserConfiguration(RequiredSpi.class, RejectingCallback.class, AnotherCallback.class)
                .run(running -> assertThat(running).hasFailed()
                        .getFailure().hasRootCauseInstanceOf(NoUniqueBeanDefinitionException.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class PrimaryCallback {

        @Bean
        @Primary
        LetterSaveCallback primaryCallback() {
            return letter -> CallbackResult.accepted();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class AnotherCallback {

        @Bean
        LetterSaveCallback anotherCallback() {
            return letter -> CallbackResult.accepted();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class RejectingCallback {

        @Bean
        LetterSaveCallback letterSaveCallback() {
            return letter -> CallbackResult.rejected("Aufgabe ist erledigt");
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class OnlyPlaceholderData {

        @Bean
        PlaceholderDataProvider placeholderDataProvider() {
            return request -> new DataContext(Map.of());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class OnlyDeletionPolicy {

        @Bean
        DeletionPolicy deletionPolicy() {
            return request -> DeletionDecision.blocked("Aufbewahrungsfrist");
        }
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
    static class CustomAuthorization {

        @Bean
        AuthorizationCheck customAuthorizationCheck() {
            return request -> true;
        }
    }
}
