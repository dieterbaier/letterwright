package io.github.dieterbaier.standardschreiben.architektur;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Belegt mit absichtlich falschen Fixture-Klassen, dass jede Regel Verstöße erkennt.
 *
 * <p>Ohne diesen Nachweis könnte eine falsch formulierte Regel dauerhaft grün sein.
 */
class ArchitekturRegelnErkennenVerstoesseTest {

    private static final String FIXTURE = "io.github.dieterbaier.standardschreiben.archfixture";

    private static JavaClasses fixtures;

    @BeforeAll
    static void importiereFixtures() {
        fixtures = new ClassFileImporter().importPackages(FIXTURE);
    }

    private static void erkenntVerstoss(ArchRule regel, String verletzendeKlasse) {
        assertThatThrownBy(() -> regel.check(fixtures))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(verletzendeKlasse);
    }

    @Test
    void domaeneMitSpringAbhaengigkeitVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.domaeneIstFrameworkfrei(FIXTURE), "DomaeneMitSpring");
    }

    @Test
    void portMitAdapterAbhaengigkeitVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.portsKennenNurDieDomaene(FIXTURE), "AusgehenderPortMitAdapter");
    }

    @Test
    void eingehenderPortMitAusgehendemPortVerletztDieRegel() {
        erkenntVerstoss(
                ArchitekturRegeln.eingehendeUndAusgehendePortsSindGetrennt(FIXTURE),
                "EingehenderPortMitAusgehendemPort");
    }

    @Test
    void serviceMitAdapterAbhaengigkeitVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.serviceKenntKeineAdapter(FIXTURE), "ServiceMitAdapter");
    }

    @Test
    void eingehenderAdapterMitAusgehendemPortVerletztDieRegel() {
        erkenntVerstoss(
                ArchitekturRegeln.eingehendeAdapterKennenNurEingehendePorts(FIXTURE),
                "WebAdapterMitAusgehendemPort");
    }

    @Test
    void ausgehenderAdapterMitEingehendemPortVerletztDieRegel() {
        erkenntVerstoss(
                ArchitekturRegeln.ausgehendeAdapterKennenNurAusgehendePorts(FIXTURE),
                "RenderingAdapterMitEingehendemPort");
    }

    @Test
    void adapterMitStarterAbhaengigkeitVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.adapterHaengenNichtVomStarterAb(FIXTURE), "RenderingAdapterMitStarter");
    }
}
