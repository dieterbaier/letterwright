package dieterbaier.letterwright.starter;

import dieterbaier.letterwright.adapter.out.host.HostPermissions;
import dieterbaier.letterwright.adapter.out.host.HostDeletionRules;
import dieterbaier.letterwright.adapter.out.host.HostPlaceholderDataSource;
import dieterbaier.letterwright.adapter.out.host.HostSaveCallback;
import dieterbaier.letterwright.spi.AuthorizationCheck;
import dieterbaier.letterwright.spi.DeletionPolicy;
import dieterbaier.letterwright.spi.LetterSaveCallback;
import dieterbaier.letterwright.spi.PlaceholderDataProvider;
import dieterbaier.letterwright.usecases.out.Permissions;
import dieterbaier.letterwright.usecases.out.DeletionRules;
import dieterbaier.letterwright.usecases.out.PlaceholderDataSource;
import dieterbaier.letterwright.usecases.out.SaveCallback;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;

/**
 * Verdrahtet die Bibliothek mit den SPI-Implementierungen des Hosts (ADR-016).
 *
 * <p>{@link PlaceholderDataProvider} und {@link DeletionPolicy} muss der Host bereitstellen; fehlt eine, startet der
 * Kontext nicht. Für die {@link AuthorizationCheck} gibt es eine Default-Implementierung, die zurücktritt, sobald der
 * Host eine eigene Bean bereitstellt. Den Rückruf {@link LetterSaveCallback} stellt der Host nur bereit, wenn er ihn
 * braucht (ADR-023).
 */
@AutoConfiguration
@EnableConfigurationProperties(AuthorizationProperties.class)
public class LetterwrightAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuthorizationCheck.class)
    RoleAuthorizationCheck roleAuthorizationCheck(AuthorizationProperties properties,
            ObjectProvider<AuthenticationTrustResolver> trust) {
        return new RoleAuthorizationCheck(properties,
                trust.getIfAvailable(AuthenticationTrustResolverImpl::new));
    }

    @Bean
    Permissions permissions(AuthorizationCheck check) {
        return new HostPermissions(check);
    }

    @Bean
    PlaceholderDataSource placeholderDataSource(PlaceholderDataProvider provider) {
        return new HostPlaceholderDataSource(provider);
    }

    @Bean
    DeletionRules deletionRules(DeletionPolicy policy) {
        return new HostDeletionRules(policy);
    }

    /**
     * Für das Speichern über die Oberfläche. Der Anwendungsfall hinter der Java-API bekommt
     * {@link HostSaveCallback#none()}, sobald er verdrahtet ist (#10).
     */
    @Bean
    SaveCallback saveCallback(ObjectProvider<LetterSaveCallback> callback) {
        // Bei mehreren Beans gilt die Auswahl von Spring, wie im Vertrag von NachDemSpeichern beschrieben:
        // getIfAvailable wählt nach den üblichen Regeln von Spring (etwa @Primary) und lässt den Kontext nicht
        // starten, wenn die Auswahl mehrdeutig bleibt.
        return Optional.ofNullable(callback.getIfAvailable()).map(HostSaveCallback::forReference)
                .orElseGet(HostSaveCallback::none);
    }
}
