package dieterbaier.standardschreiben.starter;

import dieterbaier.standardschreiben.adapter.out.host.HostBerechtigungen;
import dieterbaier.standardschreiben.adapter.out.host.HostLoeschregeln;
import dieterbaier.standardschreiben.adapter.out.host.HostPlatzhalterdatenquelle;
import dieterbaier.standardschreiben.adapter.out.host.HostSpeicherrueckruf;
import dieterbaier.standardschreiben.spi.Berechtigungspruefung;
import dieterbaier.standardschreiben.spi.Loeschrichtlinie;
import dieterbaier.standardschreiben.spi.NachDemSpeichern;
import dieterbaier.standardschreiben.spi.PlatzhalterdatenAnbieter;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.Loeschregeln;
import dieterbaier.standardschreiben.usecases.out.Platzhalterdatenquelle;
import dieterbaier.standardschreiben.usecases.out.Speicherrueckruf;
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
 * <p>{@link PlatzhalterdatenAnbieter} und {@link Loeschrichtlinie} muss der Host bereitstellen; fehlt eine, startet der
 * Kontext nicht. Für die {@link Berechtigungspruefung} gibt es eine Default-Implementierung, die zurücktritt, sobald der
 * Host eine eigene Bean bereitstellt. Den Rückruf {@link NachDemSpeichern} stellt der Host nur bereit, wenn er ihn
 * braucht (ADR-023).
 */
@AutoConfiguration
@EnableConfigurationProperties(Berechtigungseigenschaften.class)
public class StandardschreibenAutoKonfiguration {

    @Bean
    @ConditionalOnMissingBean(Berechtigungspruefung.class)
    RollenBerechtigungspruefung rollenBerechtigungspruefung(Berechtigungseigenschaften eigenschaften,
            ObjectProvider<AuthenticationTrustResolver> vertrauen) {
        return new RollenBerechtigungspruefung(eigenschaften,
                vertrauen.getIfAvailable(AuthenticationTrustResolverImpl::new));
    }

    @Bean
    Berechtigungen berechtigungen(Berechtigungspruefung pruefung) {
        return new HostBerechtigungen(pruefung);
    }

    @Bean
    Platzhalterdatenquelle platzhalterdatenquelle(PlatzhalterdatenAnbieter anbieter) {
        return new HostPlatzhalterdatenquelle(anbieter);
    }

    @Bean
    Loeschregeln loeschregeln(Loeschrichtlinie richtlinie) {
        return new HostLoeschregeln(richtlinie);
    }

    /**
     * Für das Speichern über die Oberfläche. Der Anwendungsfall hinter der Java-API bekommt
     * {@link HostSpeicherrueckruf#ohne()}, sobald er verdrahtet ist (#10).
     */
    @Bean
    Speicherrueckruf speicherrueckruf(ObjectProvider<NachDemSpeichern> rueckruf) {
        // Bei mehreren Beans gilt die Auswahl von Spring, wie im Vertrag von NachDemSpeichern beschrieben:
        // getIfAvailable nimmt die mit @Primary und lässt den Kontext ohne sie nicht starten.
        return Optional.ofNullable(rueckruf.getIfAvailable()).map(HostSpeicherrueckruf::zu)
                .orElseGet(HostSpeicherrueckruf::ohne);
    }
}
