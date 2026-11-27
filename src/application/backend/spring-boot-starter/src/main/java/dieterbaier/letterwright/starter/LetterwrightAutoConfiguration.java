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
 * Wires the library with the host's SPI implementations (ADR-016).
 *
 * <p>The host must provide {@link PlaceholderDataProvider} and {@link DeletionPolicy}; if one is missing, the context
 * does not start. For the {@link AuthorizationCheck} there is a default implementation that steps back as soon as the
 * host provides a bean of its own. The host provides the callback {@link LetterSaveCallback} only if it needs it
 * (ADR-023).
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
     * For saving through the user interface. The use case behind the Java API gets
     * {@link HostSaveCallback#none()} as soon as it is wired (#10).
     */
    @Bean
    SaveCallback saveCallback(ObjectProvider<LetterSaveCallback> callback) {
        // With several beans Spring's selection applies, as described in the contract of LetterSaveCallback:
        // getIfAvailable selects by Spring's usual rules (for example @Primary) and prevents the context from
        // starting if the selection remains ambiguous.
        return Optional.ofNullable(callback.getIfAvailable()).map(HostSaveCallback::forReference)
                .orElseGet(HostSaveCallback::none);
    }
}
