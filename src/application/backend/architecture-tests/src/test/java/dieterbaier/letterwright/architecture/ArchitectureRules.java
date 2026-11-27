package dieterbaier.letterwright.architecture;

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
 * Rules of the hexagonal architecture (ADR-002), parameterized by the base package.
 *
 * <p>The module boundaries already enforce most rules at compile time. The rules here
 * additionally check them at package level, in case a dependency in a build script is
 * declared too broadly.
 *
 * <p>The parameterization makes it possible to check the rules against fixture classes and so
 * prove that they actually detect violations.
 *
 * <p>No rule allows an empty selector. If a selector no longer matches any class after a rename,
 * the check fails instead of silently staying green (ADR-015). So that a module without further
 * classes is matched too, {@code -Xpkginfo:always} compiles every {@code package-info}.
 * A selector over several packages stays green as long as one of them still matches.
 */
final class ArchitectureRules {

    private ArchitectureRules() {
    }

    /** The domain depends neither on Spring nor on use cases, adapters or the starter. */
    static ArchRule domainIsFrameworkFree(String base) {
        return noClasses()
                .that().resideInAPackage(base + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        base + ".usecases..",
                        base + ".adapter..",
                        base + ".starter..")
                .because("the domain is the framework-free core (ADR-002)");
    }

    /**
     * Besides the JDK, the domain uses only explicitly approved libraries: JSpecify (ADR-015), the
     * DDD annotations of jMolecules (ADR-018) and the library's own validation module. Every further one needs a
     * decision according to ADR-002.
     */
    static ArchRule domainUsesOnlyApprovedLibraries(String base) {
        return classes()
                .that().resideInAPackage(base + ".domain..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..",
                        "org.jspecify.annotations..",
                        "org.jmolecules.ddd.annotation..",
                        base + ".domain..",
                        base + ".validation..")
                .because("every library in the domain is a decision of its own (ADR-002, ADR-018)");
    }

    /**
     * Every aggregate and every entity marks its identity with {@code @Identity}, on the field or on the method
     * that returns it (ADR-018). Otherwise neither a reader nor a later evaluation of the annotations finds it.
     */
    static ArchRule aggregatesAndEntitiesMarkTheirIdentity(String base) {
        return classes()
                .that().resideInAPackage(base + ".domain..")
                .and().areAnnotatedWith(AGGREGATE_ROOT)
                .or().resideInAPackage(base + ".domain..")
                .and().areAnnotatedWith(ENTITY)
                .should(new ArchCondition<JavaClass>("have a field or a method with @Identity") {
                    @Override
                    public void check(JavaClass type, ConditionEvents events) {
                        boolean marked = type.getFields().stream()
                                        .anyMatch(field -> field.isAnnotatedWith(IDENTITY))
                                || type.getMethods().stream()
                                        .anyMatch(method -> method.isAnnotatedWith(IDENTITY));
                        events.add(new SimpleConditionEvent(type, marked,
                                type.getName() + " has no field and no method with @Identity"));
                    }
                })
                .because("the identity determines the equality of aggregates and entities (ADR-018)");
    }

    private static final String AGGREGATE_ROOT = "org.jmolecules.ddd.annotation.AggregateRoot";
    private static final String ENTITY = "org.jmolecules.ddd.annotation.Entity";
    private static final String IDENTITY = "org.jmolecules.ddd.annotation.Identity";

    /** The validation module is general and knows no other package of the library. */
    static ArchRule validationIsSelfContained(String base) {
        return noClasses()
                .that().resideInAPackage(base + ".validation..")
                .should().dependOnClassesThat(
                        resideInAPackage("org.springframework..")
                                .or(resideInAPackage(base + "..")
                                        .and(resideOutsideOfPackage(base + ".validation.."))))
                .because("the checks are general; business rules stay in the domain (ADR-018)");
    }

    /** The ports are framework-free and know only the domain, not the service, adapters or the starter. */
    static ArchRule portsKnowOnlyTheDomain(String base) {
        return noClasses()
                .that().resideInAnyPackage(base + ".usecases.in..", base + ".usecases.out..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        base + ".usecases.service..",
                        base + ".adapter..",
                        base + ".starter..")
                .because("ports are the contract with adapters and host and depend only on the domain (ADR-002)");
    }

    /** Inbound and outbound ports do not know each other. */
    static ArchRule inboundAndOutboundPortsAreSeparate(String base) {
        return CompositeArchRule
                .of(noClasses()
                        .that().resideInAPackage(base + ".usecases.in..")
                        .should().dependOnClassesThat().resideInAPackage(base + ".usecases.out..")
                        .because("an inbound port describes a use case, not an SPI (ADR-002)"))
                .and(noClasses()
                        .that().resideInAPackage(base + ".usecases.out..")
                        .should().dependOnClassesThat().resideInAPackage(base + ".usecases.in..")
                        .because("an outbound port does not call use cases (ADR-002)"));
    }

    /** The implementation of the use cases knows no adapters and no starter, only ports. */
    static ArchRule serviceKnowsNoAdapters(String base) {
        return noClasses()
                .that().resideInAPackage(base + ".usecases.service..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        base + ".adapter..",
                        base + ".starter..")
                .because("adapters implement ports, not the other way round (ADR-002)");
    }

    /** Inbound adapters call only inbound ports. */
    static ArchRule inboundAdaptersKnowOnlyInboundPorts(String base) {
        return noClasses()
                .that().resideInAPackage(base + ".adapter.in..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        base + ".usecases.out..",
                        base + ".usecases.service..")
                .because("inbound adapters address the application only through its use cases (ADR-002)");
    }

    /** Outbound adapters implement only outbound ports. */
    static ArchRule outboundAdaptersKnowOnlyOutboundPorts(String base) {
        return noClasses()
                .that().resideInAPackage(base + ".adapter.out..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        base + ".usecases.in..",
                        base + ".usecases.service..")
                .because("outbound adapters are called by the application, not the other way round (ADR-002)");
    }

    /** Adapters do not depend on the starter; only the starter wires them. */
    static ArchRule adaptersDoNotDependOnTheStarter(String base) {
        return noClasses()
                .that().resideInAPackage(base + ".adapter..")
                .should().dependOnClassesThat().resideInAPackage(base + ".starter..")
                .because("only the starter wires adapters (ADR-002)");
    }

    /** The domain model knows no domain services; the dependency runs only from the service to the model. */
    static ArchRule domainModelKnowsNoDomainServices(String base) {
        return noClasses()
                .that().resideInAPackage(base + ".domain.model..")
                .should().dependOnClassesThat().resideInAPackage(base + ".domain.service..")
                .because("domain services work on the model, not the other way round (ADR-015)");
    }

    /**
     * The aggregate packages in the domain model form no cycle (ADR-018). An aggregate may know another one through its
     * identity or value types, but not the other way round at the same time.
     */
    static ArchRule aggregatePackagesAreFreeOfCycles(String base) {
        return slices().matching(base + ".domain.model.(*)..")
                .should().beFreeOfCycles()
                .because("every aggregate should stay understandable and changeable on its own (ADR-018)");
    }

    /**
     * The root package of the domain model contains the value types that several aggregates and the ports share. It knows
     * none of the aggregate packages, otherwise every aggregate would depend on all others through the shared types (ADR-018).
     */
    static ArchRule sharedValueTypesKnowNoAggregates(String base) {
        return noClasses()
                .that().resideInAPackage(base + ".domain.model")
                .should().dependOnClassesThat(resideInAPackage(base + ".domain.model..")
                        .and(resideOutsideOfPackage(base + ".domain.model")))
                .because("shared value types belong to no aggregate (ADR-018)");
    }

    /** Ports and adapters know no domain services; only the implementation of the use cases uses them. */
    static ArchRule onlyTheUseCasesKnowDomainServices(String base) {
        return noClasses()
                .that().resideInAnyPackage(
                        base + ".usecases.in..",
                        base + ".usecases.out..",
                        base + ".adapter..")
                .should().dependOnClassesThat().resideInAPackage(base + ".domain.service..")
                .because("domain services are reachable only through the use cases (ADR-015)");
    }

    /**
     * The SPI is plain Java and depends on no other package of the library.
     *
     * <p>Every package under the base package outside {@code spi} is forbidden, not only the ones
     * known today. So the rule stays red for a package added later, too.
     */
    static ArchRule spiIsSelfContained(String base) {
        return noClasses()
                .that().resideInAPackage(base + ".spi..")
                .should().dependOnClassesThat(
                        resideInAPackage("org.springframework..")
                                .or(resideInAPackage(base + "..")
                                        .and(resideOutsideOfPackage(base + ".spi.."))))
                .because("the SPI is the only contract with the host and has its own data types (ADR-016)");
    }

    /** Domain, ports and use cases do not know the SPI. */
    static ArchRule coreDoesNotKnowTheSpi(String base) {
        return noClasses()
                .that().resideInAnyPackage(base + ".domain..", base + ".usecases..")
                .should().dependOnClassesThat().resideInAPackage(base + ".spi..")
                .because("the core asks the host through outbound ports in domain terms (ADR-016)");
    }

    /** Apart from the SPI itself, only adapter-host and the starter use the SPI. */
    static ArchRule onlyAdapterHostAndStarterKnowTheSpi(String base) {
        return noClasses()
                .that().resideInAPackage(base + "..")
                .and().resideOutsideOfPackages(
                        base + ".spi..",
                        base + ".adapter.out.host..",
                        base + ".starter..")
                .should().dependOnClassesThat().resideInAPackage(base + ".spi..")
                .because("only adapter-host maps between SPI and domain, only the starter wires (ADR-016)");
    }

    /**
     * The Java API is plain Java and depends on no other package of the library, not even on the SPI (ADR-023).
     *
     * <p>As with the SPI, every package under the base package outside {@code api} is forbidden, so that the rule stays
     * red for a package added later, too.
     */
    static ArchRule apiIsSelfContained(String base) {
        return noClasses()
                .that().resideInAPackage(base + ".api..")
                .should().dependOnClassesThat(
                        resideInAPackage("org.springframework..")
                                .or(resideInAPackage(base + "..")
                                        .and(resideOutsideOfPackage(base + ".api.."))))
                .because("the Java API is a contract with the host and has its own data types (ADR-016, ADR-023)");
    }

    /** Apart from the API itself, only adapter-api and the starter use the Java API. */
    static ArchRule onlyAdapterApiAndStarterKnowTheApi(String base) {
        return noClasses()
                .that().resideInAPackage(base + "..")
                .and().resideOutsideOfPackages(
                        base + ".api..",
                        base + ".adapter.in.api..",
                        base + ".starter..")
                .should().dependOnClassesThat().resideInAPackage(base + ".api..")
                .because("only adapter-api maps between API and domain, only the starter wires (ADR-023)");
    }

    /** No adapter uses another; each addresses the application only through its ports. */
    static ArchRule adaptersAreIsolatedFromEachOther(String base) {
        return slices()
                .matching(base + ".adapter.(*).(*)..")
                .namingSlices("Adapter $1.$2")
                .should().notDependOnEachOther()
                .because("an adapter bypassing the use cases undermines the ports (ADR-015)");
    }
}
