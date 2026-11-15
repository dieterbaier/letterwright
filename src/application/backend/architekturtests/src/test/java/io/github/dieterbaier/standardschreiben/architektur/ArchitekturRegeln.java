package io.github.dieterbaier.standardschreiben.architektur;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;

/**
 * Regeln der hexagonalen Architektur (ADR-002), parametrisiert über das Basispaket.
 *
 * <p>Die Modulgrenzen erzwingen die meisten Regeln bereits beim Kompilieren. Die Regeln hier
 * prüfen sie zusätzlich auf Paketebene, falls eine Abhängigkeit in einem Build-Skript zu weit
 * gefasst wird.
 *
 * <p>Die Parametrisierung erlaubt es, die Regeln gegen Fixture-Klassen zu prüfen und so zu
 * belegen, dass sie Verstöße tatsächlich erkennen.
 */
final class ArchitekturRegeln {

    private ArchitekturRegeln() {
    }

    /** Die Domäne hängt weder von Spring noch von Use Cases, Adaptern oder Starter ab. */
    static ArchRule domaeneIstFrameworkfrei(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".domaene..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        basis + ".usecases..",
                        basis + ".adapter..",
                        basis + ".starter..")
                .because("die Domäne ist der frameworkfreie Kern (ADR-002)")
                .allowEmptyShould(true);
    }

    /** Die Ports sind frameworkfrei und kennen nur die Domäne, nicht Service, Adapter oder Starter. */
    static ArchRule portsKennenNurDieDomaene(String basis) {
        return noClasses()
                .that().resideInAnyPackage(basis + ".usecases.in..", basis + ".usecases.out..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        basis + ".usecases.service..",
                        basis + ".adapter..",
                        basis + ".starter..")
                .because("Ports sind der Vertrag zu Adaptern und Host und hängen nur an der Domäne (ADR-002)")
                .allowEmptyShould(true);
    }

    /** Eingehende und ausgehende Ports kennen einander nicht. */
    static ArchRule eingehendeUndAusgehendePortsSindGetrennt(String basis) {
        return CompositeArchRule
                .of(noClasses()
                        .that().resideInAPackage(basis + ".usecases.in..")
                        .should().dependOnClassesThat().resideInAPackage(basis + ".usecases.out..")
                        .because("ein eingehender Port beschreibt einen Anwendungsfall, kein SPI (ADR-002)")
                        .allowEmptyShould(true))
                .and(noClasses()
                        .that().resideInAPackage(basis + ".usecases.out..")
                        .should().dependOnClassesThat().resideInAPackage(basis + ".usecases.in..")
                        .because("ein ausgehender Port ruft keine Anwendungsfälle auf (ADR-002)")
                        .allowEmptyShould(true));
    }

    /** Die Implementierung der Anwendungsfälle kennt keine Adapter und keinen Starter, nur Ports. */
    static ArchRule serviceKenntKeineAdapter(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".usecases.service..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basis + ".adapter..",
                        basis + ".starter..")
                .because("Adapter implementieren Ports, nicht umgekehrt (ADR-002)")
                .allowEmptyShould(true);
    }

    /** Eingehende Adapter rufen nur eingehende Ports auf. */
    static ArchRule eingehendeAdapterKennenNurEingehendePorts(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".adapter.in..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basis + ".usecases.out..",
                        basis + ".usecases.service..")
                .because("eingehende Adapter sprechen die Anwendung nur über ihre Anwendungsfälle an (ADR-002)")
                .allowEmptyShould(true);
    }

    /** Ausgehende Adapter implementieren nur ausgehende Ports. */
    static ArchRule ausgehendeAdapterKennenNurAusgehendePorts(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".adapter.out..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basis + ".usecases.in..",
                        basis + ".usecases.service..")
                .because("ausgehende Adapter werden von der Anwendung gerufen, nicht umgekehrt (ADR-002)")
                .allowEmptyShould(true);
    }

    /** Adapter hängen nicht vom Starter ab; nur der Starter verdrahtet sie. */
    static ArchRule adapterHaengenNichtVomStarterAb(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".adapter..")
                .should().dependOnClassesThat().resideInAPackage(basis + ".starter..")
                .because("nur der Starter verdrahtet Adapter (ADR-002)")
                .allowEmptyShould(true);
    }
}
