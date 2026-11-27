package dieterbaier.letterwright.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Prüft den Produktionscode aller Backend-Module gegen die Regeln aus ADR-002. */
class HexagonalArchitectureTest {

    private static final String BASE = "dieterbaier.letterwright";

    private static JavaClasses productionCode;

    @BeforeAll
    static void importProductionCode() {
        productionCode = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(BASE);
    }

    @Test
    void domainIsFrameworkFree() {
        ArchitectureRules.domainIsFrameworkFree(BASE).check(productionCode);
    }

    @Test
    void domainUsesOnlyApprovedLibraries() {
        ArchitectureRules.domainUsesOnlyApprovedLibraries(BASE).check(productionCode);
    }

    @Test
    void aggregatePackagesAreFreeOfCycles() {
        ArchitectureRules.aggregatePackagesAreFreeOfCycles(BASE).check(productionCode);
    }

    @Test
    void sharedValueTypesKnowNoAggregates() {
        ArchitectureRules.sharedValueTypesKnowNoAggregates(BASE).check(productionCode);
    }

    @Test
    void aggregatesAndEntitiesMarkTheirIdentity() {
        ArchitectureRules.aggregatesAndEntitiesMarkTheirIdentity(BASE).check(productionCode);
    }

    @Test
    void validationIsSelfContained() {
        ArchitectureRules.validationIsSelfContained(BASE).check(productionCode);
    }

    @Test
    void portsKnowOnlyTheDomain() {
        ArchitectureRules.portsKnowOnlyTheDomain(BASE).check(productionCode);
    }

    @Test
    void inboundAndOutboundPortsAreSeparate() {
        ArchitectureRules.inboundAndOutboundPortsAreSeparate(BASE).check(productionCode);
    }

    @Test
    void serviceKnowsNoAdapters() {
        ArchitectureRules.serviceKnowsNoAdapters(BASE).check(productionCode);
    }

    @Test
    void inboundAdaptersKnowOnlyInboundPorts() {
        ArchitectureRules.inboundAdaptersKnowOnlyInboundPorts(BASE).check(productionCode);
    }

    @Test
    void outboundAdaptersKnowOnlyOutboundPorts() {
        ArchitectureRules.outboundAdaptersKnowOnlyOutboundPorts(BASE).check(productionCode);
    }

    @Test
    void adaptersDoNotDependOnTheStarter() {
        ArchitectureRules.adaptersDoNotDependOnTheStarter(BASE).check(productionCode);
    }

    @Test
    void domainModelKnowsNoDomainServices() {
        ArchitectureRules.domainModelKnowsNoDomainServices(BASE).check(productionCode);
    }

    @Test
    void onlyTheUseCasesKnowDomainServices() {
        ArchitectureRules.onlyTheUseCasesKnowDomainServices(BASE).check(productionCode);
    }

    @Test
    void spiIsSelfContained() {
        ArchitectureRules.spiIsSelfContained(BASE).check(productionCode);
    }

    @Test
    void coreDoesNotKnowTheSpi() {
        ArchitectureRules.coreDoesNotKnowTheSpi(BASE).check(productionCode);
    }

    @Test
    void onlyAdapterHostAndStarterKnowTheSpi() {
        ArchitectureRules.onlyAdapterHostAndStarterKnowTheSpi(BASE).check(productionCode);
    }

    @Test
    void apiIsSelfContained() {
        ArchitectureRules.apiIsSelfContained(BASE).check(productionCode);
    }

    @Test
    void onlyAdapterApiAndStarterKnowTheApi() {
        ArchitectureRules.onlyAdapterApiAndStarterKnowTheApi(BASE).check(productionCode);
    }

    @Test
    void adaptersAreIsolatedFromEachOther() {
        ArchitectureRules.adaptersAreIsolatedFromEachOther(BASE).check(productionCode);
    }
}
