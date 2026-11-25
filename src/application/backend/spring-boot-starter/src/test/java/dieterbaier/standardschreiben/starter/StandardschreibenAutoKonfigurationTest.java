package dieterbaier.standardschreiben.starter;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreibenzustand;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.Berechtigungspruefung;
import dieterbaier.standardschreiben.spi.Datenkontext;
import dieterbaier.standardschreiben.spi.Loeschentscheidung;
import dieterbaier.standardschreiben.spi.Loeschrichtlinie;
import dieterbaier.standardschreiben.spi.NachDemSpeichern;
import dieterbaier.standardschreiben.spi.Rueckrufergebnis;
import dieterbaier.standardschreiben.spi.PlatzhalterdatenAnbieter;
import dieterbaier.standardschreiben.spi.SchreibenAktion;
import dieterbaier.standardschreiben.spi.Standardschreibenkennung;
import dieterbaier.standardschreiben.spi.VorlagenAktion;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.Loeschregeln;
import dieterbaier.standardschreiben.usecases.out.Platzhalterdatenquelle;
import dieterbaier.standardschreiben.usecases.out.Speicherrueckruf;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Zugleich Brücke zum Szenario „Ohne Rückruf des Hosts nimmt die Bibliothek jedes neue Schreiben an“ in rueckruf-nach-dem-speichern.feature. */
class StandardschreibenAutoKonfigurationTest {

    private static final dieterbaier.standardschreiben.spi.FachlicherBezug KUNDE =
            new dieterbaier.standardschreiben.spi.FachlicherBezug("Kunde", "4711");
    private static final Standardschreibenkennung MAHNUNG = new Standardschreibenkennung(UUID.randomUUID(), "Mahnung");

    private static final BenutzerKennung ERIKA = new BenutzerKennung("erika");
    private static final Schreiben SCHREIBEN = Schreiben.wiederherstellen(SchreibenId.neu(), SnapshotId.neu(),
            new FachlicherBezug("Kunde", "4711"), new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE),
            new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE), new Dokumentverweis("ablage/1"),
            Pruefsumme.ueber(new byte[] {1}), Instant.parse("2026-11-25T10:00:00Z"), new Schreibenzustand.Sichtbar());

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
                    .darf(new BenutzerKennung("erika"), dieterbaier.standardschreiben.domaene.modell.SchreibenAktion
                            .LOESCHEN, new FachlicherBezug("Kunde", "4711")))
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
                .withPropertyValues("standardschreiben.berechtigung.rollen.schreiben.ERZEUGEN=ROLE_SACHBEARBEITUNG",
                        "standardschreiben.berechtigung.rollen.vorlagen.BEARBEITEN=ROLE_REDAKTION")
                .run(laufend -> {
                    Berechtigungseigenschaften eigenschaften = laufend.getBean(Berechtigungseigenschaften.class);
                    assertThat(eigenschaften.rolle(Berechtigungsanfrage.zumErzeugen("erika", KUNDE, MAHNUNG))).isEqualTo("ROLE_SACHBEARBEITUNG");
                    assertThat(eigenschaften.rolle(Berechtigungsanfrage.zuSchreiben("erika", SchreibenAktion.LOESCHEN, KUNDE))).isEqualTo("ROLE_SSW_SCHREIBEN_LOESCHEN");
                    assertThat(eigenschaften.rolle(Berechtigungsanfrage.zuVorlagen("erika", VorlagenAktion.BEARBEITEN))).isEqualTo("ROLE_REDAKTION");
                    assertThat(eigenschaften.rolle(Berechtigungsanfrage.zuVorlagen("erika", VorlagenAktion.VEROEFFENTLICHEN)))
                            .isEqualTo("ROLE_SSW_VORLAGEN_VEROEFFENTLICHEN");
                });
    }

    @Test
    void ohneRueckrufDesHostsNimmtDieBibliothekJedesNeueSchreibenAn() {
        kontext.withUserConfiguration(PflichtSpi.class).run(laufend -> assertThat(
                laufend.getBean(Speicherrueckruf.class).angelegt(ERIKA, SCHREIBEN))
                .isEqualTo(dieterbaier.standardschreiben.domaene.modell.schreiben.Rueckrufergebnis.angenommen()));
    }

    @Test
    void einRueckrufDesHostsWirdGefragt() {
        kontext.withUserConfiguration(PflichtSpi.class, AblehnenderRueckruf.class).run(laufend -> assertThat(
                laufend.getBean(Speicherrueckruf.class).angelegt(ERIKA, SCHREIBEN))
                .isEqualTo(dieterbaier.standardschreiben.domaene.modell.schreiben.Rueckrufergebnis.abgelehnt(
                        "Aufgabe ist erledigt")));
    }

    @Configuration(proxyBeanMethods = false)
    static class AblehnenderRueckruf {

        @Bean
        NachDemSpeichern nachDemSpeichern() {
            return schreiben -> Rueckrufergebnis.abgelehnt("Aufgabe ist erledigt");
        }
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
