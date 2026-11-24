package dieterbaier.standardschreiben.architektur;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
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
 *
 * <p>Keine Regel lässt einen leeren Selektor zu. Trifft ein Selektor nach einer Umbenennung keine
 * Klasse mehr, schlägt die Prüfung fehl, statt still grün zu bleiben (ADR-015). Damit auch ein Modul
 * ohne weitere Klassen getroffen wird, kompiliert {@code -Xpkginfo:always} jede {@code package-info}.
 * Ein Selektor über mehrere Pakete bleibt grün, solange eines davon noch trifft.
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
                .because("die Domäne ist der frameworkfreie Kern (ADR-002)");
    }

    /**
     * Die Domäne benutzt außer dem JDK nur ausdrücklich zugelassene Bibliotheken: JSpecify (ADR-015), die
     * DDD-Annotationen von jMolecules (ADR-018) und das eigene Prüfmodul. Jede weitere braucht eine Entscheidung
     * nach ADR-002.
     */
    static ArchRule domaeneNutztNurZugelasseneBibliotheken(String basis) {
        return classes()
                .that().resideInAPackage(basis + ".domaene..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..",
                        "org.jspecify.annotations..",
                        "org.jmolecules.ddd.annotation..",
                        basis + ".domaene..",
                        basis + ".validierung..")
                .because("jede Bibliothek in der Domäne ist eine einzelne Entscheidung (ADR-002, ADR-018)");
    }

    /**
     * Jedes Aggregat und jede Entität kennzeichnet ihre Identität mit {@code @Identity}, am Feld oder an der Methode,
     * die sie liefert (ADR-018). Sonst findet weder ein Leser noch eine spätere Auswertung der Annotationen sie.
     */
    static ArchRule aggregateUndEntitaetenKennzeichnenIhreIdentitaet(String basis) {
        return classes()
                .that().resideInAPackage(basis + ".domaene..")
                .and().areAnnotatedWith(AGGREGATE_ROOT)
                .or().resideInAPackage(basis + ".domaene..")
                .and().areAnnotatedWith(ENTITAET)
                .should(new ArchCondition<JavaClass>("ein Feld oder eine Methode mit @Identity haben") {
                    @Override
                    public void check(JavaClass klasse, ConditionEvents ereignisse) {
                        boolean gekennzeichnet = klasse.getFields().stream()
                                        .anyMatch(feld -> feld.isAnnotatedWith(IDENTITAET))
                                || klasse.getMethods().stream()
                                        .anyMatch(methode -> methode.isAnnotatedWith(IDENTITAET));
                        ereignisse.add(new SimpleConditionEvent(klasse, gekennzeichnet,
                                klasse.getName() + " hat kein Feld und keine Methode mit @Identity"));
                    }
                })
                .because("die Identität bestimmt die Gleichheit von Aggregaten und Entitäten (ADR-018)");
    }

    private static final String AGGREGATE_ROOT = "org.jmolecules.ddd.annotation.AggregateRoot";
    private static final String ENTITAET = "org.jmolecules.ddd.annotation.Entity";
    private static final String IDENTITAET = "org.jmolecules.ddd.annotation.Identity";

    /** Das Prüfmodul ist allgemein und kennt kein anderes Paket der Bibliothek. */
    static ArchRule validierungIstEigenstaendig(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".validierung..")
                .should().dependOnClassesThat(
                        resideInAPackage("org.springframework..")
                                .or(resideInAPackage(basis + "..")
                                        .and(resideOutsideOfPackage(basis + ".validierung.."))))
                .because("die Prüfungen sind allgemein; fachliche Regeln bleiben in der Domäne (ADR-018)");
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
                .because("Ports sind der Vertrag zu Adaptern und Host und hängen nur an der Domäne (ADR-002)");
    }

    /** Eingehende und ausgehende Ports kennen einander nicht. */
    static ArchRule eingehendeUndAusgehendePortsSindGetrennt(String basis) {
        return CompositeArchRule
                .of(noClasses()
                        .that().resideInAPackage(basis + ".usecases.in..")
                        .should().dependOnClassesThat().resideInAPackage(basis + ".usecases.out..")
                        .because("ein eingehender Port beschreibt einen Anwendungsfall, kein SPI (ADR-002)"))
                .and(noClasses()
                        .that().resideInAPackage(basis + ".usecases.out..")
                        .should().dependOnClassesThat().resideInAPackage(basis + ".usecases.in..")
                        .because("ein ausgehender Port ruft keine Anwendungsfälle auf (ADR-002)"));
    }

    /** Die Implementierung der Anwendungsfälle kennt keine Adapter und keinen Starter, nur Ports. */
    static ArchRule serviceKenntKeineAdapter(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".usecases.service..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basis + ".adapter..",
                        basis + ".starter..")
                .because("Adapter implementieren Ports, nicht umgekehrt (ADR-002)");
    }

    /** Eingehende Adapter rufen nur eingehende Ports auf. */
    static ArchRule eingehendeAdapterKennenNurEingehendePorts(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".adapter.in..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basis + ".usecases.out..",
                        basis + ".usecases.service..")
                .because("eingehende Adapter sprechen die Anwendung nur über ihre Anwendungsfälle an (ADR-002)");
    }

    /** Ausgehende Adapter implementieren nur ausgehende Ports. */
    static ArchRule ausgehendeAdapterKennenNurAusgehendePorts(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".adapter.out..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basis + ".usecases.in..",
                        basis + ".usecases.service..")
                .because("ausgehende Adapter werden von der Anwendung gerufen, nicht umgekehrt (ADR-002)");
    }

    /** Adapter hängen nicht vom Starter ab; nur der Starter verdrahtet sie. */
    static ArchRule adapterHaengenNichtVomStarterAb(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".adapter..")
                .should().dependOnClassesThat().resideInAPackage(basis + ".starter..")
                .because("nur der Starter verdrahtet Adapter (ADR-002)");
    }

    /** Das Domänenmodell kennt keine Domänenservices; die Abhängigkeit läuft nur vom Service zum Modell. */
    static ArchRule domaenenmodellKenntKeineDomaenenservices(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".domaene.modell..")
                .should().dependOnClassesThat().resideInAPackage(basis + ".domaene.service..")
                .because("Domänenservices arbeiten auf dem Modell, nicht umgekehrt (ADR-015)");
    }

    /**
     * Die Pakete der Aggregate im Fachmodell bilden keinen Zyklus (ADR-018). Ein Aggregat darf ein anderes über dessen
     * Identität oder Werttypen kennen, aber nicht umgekehrt zugleich.
     */
    static ArchRule aggregatpaketeSindZyklenfrei(String basis) {
        return slices().matching(basis + ".domaene.modell.(*)..")
                .should().beFreeOfCycles()
                .because("jedes Aggregat soll für sich verständlich und änderbar bleiben (ADR-018)");
    }

    /**
     * Das Wurzelpaket des Fachmodells enthält die Werttypen, die mehrere Aggregate und die Ports teilen. Es kennt keines
     * der Aggregatpakete, sonst hinge jedes Aggregat über die geteilten Typen an allen anderen (ADR-018).
     */
    static ArchRule geteilteWerttypenKennenKeineAggregate(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".domaene.modell")
                .should().dependOnClassesThat(resideInAPackage(basis + ".domaene.modell..")
                        .and(resideOutsideOfPackage(basis + ".domaene.modell")))
                .because("geteilte Werttypen gehören keinem Aggregat (ADR-018)");
    }

    /** Ports und Adapter kennen keine Domänenservices; nur die Implementierung der Anwendungsfälle nutzt sie. */
    static ArchRule nurDieAnwendungsfaelleKennenDomaenenservices(String basis) {
        return noClasses()
                .that().resideInAnyPackage(
                        basis + ".usecases.in..",
                        basis + ".usecases.out..",
                        basis + ".adapter..")
                .should().dependOnClassesThat().resideInAPackage(basis + ".domaene.service..")
                .because("Domänenservices sind nur über die Anwendungsfälle erreichbar (ADR-015)");
    }

    /**
     * Das SPI ist reines Java und hängt an keinem anderen Paket der Bibliothek.
     *
     * <p>Verboten ist jedes Paket unter dem Basispaket außerhalb von {@code spi}, nicht nur die
     * heute bekannten. So bleibt die Regel auch für ein später hinzukommendes Paket rot.
     */
    static ArchRule spiIstEigenstaendig(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".spi..")
                .should().dependOnClassesThat(
                        resideInAPackage("org.springframework..")
                                .or(resideInAPackage(basis + "..")
                                        .and(resideOutsideOfPackage(basis + ".spi.."))))
                .because("das SPI ist der einzige Vertrag mit dem Host und hat eigene Datentypen (ADR-016)");
    }

    /** Domäne, Ports und Anwendungsfälle kennen das SPI nicht. */
    static ArchRule kernKenntDasSpiNicht(String basis) {
        return noClasses()
                .that().resideInAnyPackage(basis + ".domaene..", basis + ".usecases..")
                .should().dependOnClassesThat().resideInAPackage(basis + ".spi..")
                .because("der Kern fragt den Host über ausgehende Ports in Domänenbegriffen an (ADR-016)");
    }

    /** Außer dem SPI selbst benutzen nur adapter-host und der Starter das SPI. */
    static ArchRule nurAdapterHostUndStarterKennenDasSpi(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + "..")
                .and().resideOutsideOfPackages(
                        basis + ".spi..",
                        basis + ".adapter.out.host..",
                        basis + ".starter..")
                .should().dependOnClassesThat().resideInAPackage(basis + ".spi..")
                .because("nur adapter-host bildet zwischen SPI und Domäne ab, nur der Starter verdrahtet (ADR-016)");
    }

    /**
     * Die Java-API ist reines Java und hängt an keinem anderen Paket der Bibliothek, auch nicht am SPI (ADR-023).
     *
     * <p>Verboten ist wie beim SPI jedes Paket unter dem Basispaket außerhalb von {@code api}, damit die Regel auch für
     * ein später hinzukommendes Paket rot bleibt.
     */
    static ArchRule apiIstEigenstaendig(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + ".api..")
                .should().dependOnClassesThat(
                        resideInAPackage("org.springframework..")
                                .or(resideInAPackage(basis + "..")
                                        .and(resideOutsideOfPackage(basis + ".api.."))))
                .because("die Java-API ist ein Vertrag mit dem Host und hat eigene Datentypen (ADR-016, ADR-023)");
    }

    /** Außer der API selbst benutzen nur adapter-api und der Starter die Java-API. */
    static ArchRule nurAdapterApiUndStarterKennenDieApi(String basis) {
        return noClasses()
                .that().resideInAPackage(basis + "..")
                .and().resideOutsideOfPackages(
                        basis + ".api..",
                        basis + ".adapter.in.api..",
                        basis + ".starter..")
                .should().dependOnClassesThat().resideInAPackage(basis + ".api..")
                .because("nur adapter-api bildet zwischen API und Domäne ab, nur der Starter verdrahtet (ADR-023)");
    }

    /** Kein Adapter benutzt einen anderen; jeder spricht die Anwendung nur über ihre Ports an. */
    static ArchRule adapterSindVoneinanderIsoliert(String basis) {
        return slices()
                .matching(basis + ".adapter.(*).(*)..")
                .namingSlices("Adapter $1.$2")
                .should().notDependOnEachOther()
                .because("ein Adapter an den Anwendungsfällen vorbei hebelt die Ports aus (ADR-015)");
    }
}
