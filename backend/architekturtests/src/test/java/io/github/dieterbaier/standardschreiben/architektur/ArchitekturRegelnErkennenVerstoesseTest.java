package io.github.dieterbaier.standardschreiben.architektur;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Belegt mit absichtlich falschen Fixture-Klassen, dass die Regeln Verstöße erkennen.
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

    @Test
    void domaeneMitSpringAbhaengigkeitVerletztDieRegel() {
        assertThatThrownBy(() -> ArchitekturRegeln.domaeneIstFrameworkfrei(FIXTURE).check(fixtures))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("DomaeneMitSpring");
    }

    @Test
    void anwendungMitAdapterAbhaengigkeitVerletztDieRegel() {
        assertThatThrownBy(() -> ArchitekturRegeln.anwendungKenntKeineAdapter(FIXTURE).check(fixtures))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("AnwendungMitAdapter");
    }
}
