package io.github.dieterbaier.standardschreiben.architektur;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.lang.ArchRule;

/**
 * Regeln der hexagonalen Architektur (ADR-002), parametrisiert über das Basispaket.
 *
 * <p>Die Parametrisierung erlaubt es, die Regeln gegen Fixture-Klassen zu prüfen und so zu
 * belegen, dass sie Verstöße tatsächlich erkennen.
 */
final class ArchitekturRegeln {

    private ArchitekturRegeln() {
    }

    /** Die Domäne hängt weder von Spring noch von Anwendung, Adaptern oder Starter ab. */
    static ArchRule domaeneIstFrameworkfrei(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".domaene..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        basis + ".anwendung..",
                        basis + ".adapter..",
                        basis + ".starter..")
                .because("die Domäne ist der frameworkfreie Kern (ADR-002)")
                .allowEmptyShould(true);
    }

    /** Die Anwendung kennt keine Adapter und keinen Starter, nur Ports. */
    static ArchRule anwendungKenntKeineAdapter(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".anwendung..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basis + ".adapter..",
                        basis + ".starter..")
                .because("Adapter implementieren Ports der Anwendung, nicht umgekehrt (ADR-002)")
                .allowEmptyShould(true);
    }

    /** Adapter hängen nicht vom Starter ab; nur der Starter verdrahtet sie. */
    static ArchRule adapterHaengenNichtVomStarterAb(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".adapter.(*)..")
                .should().dependOnClassesThat().resideInAPackage(basis + ".starter..")
                .because("nur der Starter verdrahtet Adapter (ADR-002)")
                .allowEmptyShould(true);
    }
}
