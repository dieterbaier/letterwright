package dieterbaier.letterwright.architecture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import dieterbaier.letterwright.archfixture.adapter.in.api.ApiAdapter;
import dieterbaier.letterwright.archfixture.adapter.in.web.WebAdapter;
import dieterbaier.letterwright.archfixture.adapter.out.host.HostAdapter;
import dieterbaier.letterwright.archfixture.adapter.out.rendering.RenderingAdapter;
import dieterbaier.letterwright.archfixture.api.ApiRecord;
import dieterbaier.letterwright.archfixture.api.ApiInterface;
import dieterbaier.letterwright.archfixture.spi.HostRecord;
import dieterbaier.letterwright.archfixture.spi.HostInterface;
import dieterbaier.letterwright.archfixture.usecases.in.InboundPort;
import dieterbaier.letterwright.archfixture.usecases.out.OutboundPort;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Proves with deliberately wrong fixture classes that every rule detects violations.
 *
 * <p>Without this proof a wrongly phrased rule could stay green forever.
 */
class ArchitectureRulesDetectViolationsTest {

    private static final String FIXTURE = "dieterbaier.letterwright.archfixture";

    private static JavaClasses fixtures;

    @BeforeAll
    static void importFixtures() {
        fixtures = new ClassFileImporter().importPackages(FIXTURE);
    }

    private static void detectsViolation(ArchRule rule, String violatingClass) {
        assertThatThrownBy(() -> rule.check(fixtures))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(violatingClass);
    }

    @Test
    void domainWithSpringDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.domainIsFrameworkFree(FIXTURE), "DomainWithSpring");
    }

    @Test
    void domainWithAdapterDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.domainIsFrameworkFree(FIXTURE), "DomainWithAdapter");
    }

    @Test
    void domainWithUnapprovedLibraryViolatesTheRule() {
        detectsViolation(ArchitectureRules.domainUsesOnlyApprovedLibraries(FIXTURE), "DomainWithForeignLibrary");
    }

    @Test
    void cycleBetweenAggregatePackagesViolatesTheRule() {
        detectsViolation(ArchitectureRules.aggregatePackagesAreFreeOfCycles(FIXTURE), "AggregateA");
    }

    @Test
    void sharedValueTypeWithAggregateDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.sharedValueTypesKnowNoAggregates(FIXTURE), "SharedTypeWithAggregate");
    }

    @Test
    void entityWithoutIdentityViolatesTheRule() {
        detectsViolation(ArchitectureRules.aggregatesAndEntitiesMarkTheirIdentity(FIXTURE),
                "EntityWithoutIdentity");
    }

    @Test
    void identityOnFieldOrMethodSatisfiesTheRule() {
        assertThatThrownBy(() -> ArchitectureRules.aggregatesAndEntitiesMarkTheirIdentity(FIXTURE)
                        .check(fixtures))
                .hasMessageNotContaining("AggregateWithIdentityOnField")
                .hasMessageNotContaining("EntityWithIdentityOnMethod");
    }

    @Test
    void validationWithDomainDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.validationIsSelfContained(FIXTURE), "ValidationWithDomain");
    }

    @Test
    void portWithAdapterDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.portsKnowOnlyTheDomain(FIXTURE), "OutboundPortWithAdapter");
    }

    @Test
    void inboundPortWithOutboundPortViolatesTheRule() {
        detectsViolation(
                ArchitectureRules.inboundAndOutboundPortsAreSeparate(FIXTURE),
                "InboundPortWithOutboundPort");
    }

    @Test
    void serviceWithAdapterDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.serviceKnowsNoAdapters(FIXTURE), "ServiceWithAdapter");
    }

    @Test
    void inboundAdapterWithOutboundPortViolatesTheRule() {
        detectsViolation(
                ArchitectureRules.inboundAdaptersKnowOnlyInboundPorts(FIXTURE),
                "WebAdapterWithOutboundPort");
    }

    @Test
    void outboundAdapterWithInboundPortViolatesTheRule() {
        detectsViolation(
                ArchitectureRules.outboundAdaptersKnowOnlyOutboundPorts(FIXTURE),
                "RenderingAdapterWithInboundPort");
    }

    @Test
    void adapterWithStarterDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.adaptersDoNotDependOnTheStarter(FIXTURE), "RenderingAdapterWithStarter");
    }

    @Test
    void modelWithDomainServiceViolatesTheRule() {
        detectsViolation(ArchitectureRules.domainModelKnowsNoDomainServices(FIXTURE), "ModelWithDomainService");
    }

    @Test
    void portWithDomainServiceViolatesTheRule() {
        detectsViolation(
                ArchitectureRules.onlyTheUseCasesKnowDomainServices(FIXTURE),
                "OutboundPortWithDomainService");
    }

    @Test
    void adapterWithDomainServiceViolatesTheRule() {
        detectsViolation(
                ArchitectureRules.onlyTheUseCasesKnowDomainServices(FIXTURE),
                "WebAdapterWithDomainService");
    }

    @Test
    void spiWithDomainDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.spiIsSelfContained(FIXTURE), "SpiWithDomain");
    }

    @Test
    void spiWithDependencyOnANewLibraryPackageViolatesTheRule() {
        detectsViolation(ArchitectureRules.spiIsSelfContained(FIXTURE), "SpiWithSharedPackage");
    }

    @Test
    void spiWithSpringDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.spiIsSelfContained(FIXTURE), "SpiWithSpring");
    }

    @Test
    void spiMayUseItself() {
        JavaClasses allowed = new ClassFileImporter()
                .importClasses(HostInterface.class, HostRecord.class);

        assertThatCode(() -> ArchitectureRules.spiIsSelfContained(FIXTURE).check(allowed))
                .doesNotThrowAnyException();
    }

    @Test
    void portWithSpiViolatesTheRuleForTheCore() {
        detectsViolation(ArchitectureRules.coreDoesNotKnowTheSpi(FIXTURE), "OutboundPortWithSpi");
    }

    @Test
    void otherAdapterWithSpiViolatesTheRule() {
        detectsViolation(ArchitectureRules.onlyAdapterHostAndStarterKnowTheSpi(FIXTURE), "RenderingAdapterWithSpi");
    }

    @Test
    void portWithSpiViolatesTheRuleForTheSpiUsers() {
        detectsViolation(ArchitectureRules.onlyAdapterHostAndStarterKnowTheSpi(FIXTURE), "OutboundPortWithSpi");
    }

    @Test
    void inboundAdapterWithOutboundAdapterViolatesTheRule() {
        detectsViolation(ArchitectureRules.adaptersAreIsolatedFromEachOther(FIXTURE), "WebAdapterWithRenderingAdapter");
    }

    @Test
    void adapterHostWithOtherOutboundAdapterViolatesTheRule() {
        detectsViolation(ArchitectureRules.adaptersAreIsolatedFromEachOther(FIXTURE), "HostAdapterWithRenderingAdapter");
    }

    @Test
    void adaptersWithoutMutualDependencySatisfyTheRule() {
        JavaClasses allowed = new ClassFileImporter()
                .importClasses(WebAdapter.class, RenderingAdapter.class, HostAdapter.class);

        assertThatCode(() -> ArchitectureRules.adaptersAreIsolatedFromEachOther(FIXTURE).check(allowed))
                .doesNotThrowAnyException();
    }

    @Test
    void adapterHostMayUseTheSpi() {
        JavaClasses allowed = new ClassFileImporter()
                .importClasses(HostAdapter.class, HostInterface.class, OutboundPort.class);

        assertThatCode(() -> ArchitectureRules.onlyAdapterHostAndStarterKnowTheSpi(FIXTURE).check(allowed))
                .doesNotThrowAnyException();
    }

    @Test
    void apiWithDomainDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.apiIsSelfContained(FIXTURE), "ApiWithDomain");
    }

    @Test
    void apiWithSpiDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.apiIsSelfContained(FIXTURE), "ApiWithSpi");
    }

    @Test
    void apiWithSpringDependencyViolatesTheRule() {
        detectsViolation(ArchitectureRules.apiIsSelfContained(FIXTURE), "ApiWithSpring");
    }

    @Test
    void apiMayUseItself() {
        JavaClasses allowed = new ClassFileImporter()
                .importClasses(ApiInterface.class, ApiRecord.class);

        assertThatCode(() -> ArchitectureRules.apiIsSelfContained(FIXTURE).check(allowed))
                .doesNotThrowAnyException();
    }

    @Test
    void otherAdapterWithApiViolatesTheRule() {
        detectsViolation(ArchitectureRules.onlyAdapterApiAndStarterKnowTheApi(FIXTURE), "RenderingAdapterWithApi");
    }

    @Test
    void adapterApiMayUseTheApi() {
        JavaClasses allowed = new ClassFileImporter()
                .importClasses(ApiAdapter.class, ApiInterface.class, ApiRecord.class, InboundPort.class);

        assertThatCode(() -> ArchitectureRules.onlyAdapterApiAndStarterKnowTheApi(FIXTURE).check(allowed))
                .doesNotThrowAnyException();
    }
}
