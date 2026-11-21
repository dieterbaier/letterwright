package dieterbaier.standardschreiben.starter;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.spi.Berechtigungspruefung;
import dieterbaier.standardschreiben.spi.Datenkontext;
import dieterbaier.standardschreiben.spi.Loeschentscheidung;
import dieterbaier.standardschreiben.spi.Loeschrichtlinie;
import dieterbaier.standardschreiben.spi.PlatzhalterdatenAnbieter;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.Loeschregeln;
import dieterbaier.standardschreiben.usecases.out.Platzhalterdatenquelle;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class StandardschreibenAutoKonfigurationTest {

    private final ApplicationContextRunner kontext = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(StandardschreibenAutoKonfiguration.class));

    @Test
    void ohneEigeneBerechtigungspruefungGiltDieDefaultImplementierung() {
        kontext.withUserConfiguration(PflichtSpi.class).run(laufend -> {
            assertThat(laufend).hasSingleBean(Berechtigungspruefung.class);
            assertThat(laufend.getBean(Berechtigungspruefung.class)).isInstanceOf(RollenBerechtigungspruefung.class);
            assertThat(laufend).hasSingleBean(Berechtigungen.class);
            assertThat(laufend).hasSingleBean(Platzhalterdatenquelle.class);
            assertThat(laufend).hasSingleBean(Loeschregeln.class);
        });
    }

    @Test
    void eineEigeneBerechtigungspruefungDesHostsVerdraengtDieDefaultImplementierung() {
        kontext.withUserConfiguration(PflichtSpi.class, EigeneBerechtigung.class).run(laufend -> {
            assertThat(laufend).hasSingleBean(Berechtigungspruefung.class);
            assertThat(laufend).doesNotHaveBean(RollenBerechtigungspruefung.class);
            assertThat(laufend.getBean(Berechtigungen.class)
                    .darf(new BenutzerKennung("erika"), dieterbaier.standardschreiben.domaene.modell.Aktion
                            .SCHREIBEN_LOESCHEN, new FachlicherBezug("Kunde", "4711")))
                    .isTrue();
        });
    }

    @Test
    void ohnePlatzhalterdatenAnbieterStartetDerKontextNicht() {
        kontext.withUserConfiguration(NurLoeschrichtlinie.class)
                .run(laufend -> assertThat(laufend).hasFailed()
                        .getFailure().hasMessageContaining("PlatzhalterdatenAnbieter"));
    }

    @Test
    void ohneLoeschrichtlinieStartetDerKontextNicht() {
        kontext.withUserConfiguration(NurPlatzhalterdaten.class)
                .run(laufend -> assertThat(laufend).hasFailed()
                        .getFailure().hasMessageContaining("Loeschrichtlinie"));
    }

    @Test
    void dieRollenSindKonfigurierbar() {
        kontext.withUserConfiguration(PflichtSpi.class)
                .withPropertyValues("standardschreiben.berechtigung.rollen.SCHREIBEN_ERZEUGEN=ROLE_SACHBEARBEITUNG")
                .run(laufend -> {
                    Berechtigungseigenschaften eigenschaften = laufend.getBean(Berechtigungseigenschaften.class);
                    assertThat(eigenschaften.rolle(dieterbaier.standardschreiben.spi.Aktion.SCHREIBEN_ERZEUGEN))
                            .isEqualTo("ROLE_SACHBEARBEITUNG");
                    assertThat(eigenschaften.rolle(dieterbaier.standardschreiben.spi.Aktion.SCHREIBEN_LOESCHEN))
                            .isEqualTo("ROLE_SSW_SCHREIBEN_LOESCHEN");
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class NurPlatzhalterdaten {

        @Bean
        PlatzhalterdatenAnbieter platzhalterdatenAnbieter() {
            return anfrage -> new Datenkontext(Map.of());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class NurLoeschrichtlinie {

        @Bean
        Loeschrichtlinie loeschrichtlinie() {
            return anfrage -> Loeschentscheidung.gesperrt("Aufbewahrungsfrist");
        }
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
    static class EigeneBerechtigung {

        @Bean
        Berechtigungspruefung eigeneBerechtigungspruefung() {
            return anfrage -> true;
        }
    }
}
