package dieterbaier.standardschreiben.architektur;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Prüft den Produktionscode aller Backend-Module gegen die Regeln aus ADR-002. */
class HexagonaleArchitekturTest {

    private static final String BASIS = "dieterbaier.standardschreiben";

    private static JavaClasses produktionsCode;

    @BeforeAll
    static void importiereProduktionsCode() {
        produktionsCode = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(BASIS);
    }

    @Test
    void domaeneIstFrameworkfrei() {
        ArchitekturRegeln.domaeneIstFrameworkfrei(BASIS).check(produktionsCode);
    }

    @Test
    void domaeneNutztNurZugelasseneBibliotheken() {
        ArchitekturRegeln.domaeneNutztNurZugelasseneBibliotheken(BASIS).check(produktionsCode);
    }

    @Test
    void aggregatpaketeSindZyklenfrei() {
        ArchitekturRegeln.aggregatpaketeSindZyklenfrei(BASIS).check(produktionsCode);
    }

    @Test
    void geteilteWerttypenKennenKeineAggregate() {
        ArchitekturRegeln.geteilteWerttypenKennenKeineAggregate(BASIS).check(produktionsCode);
    }

    @Test
    void aggregateUndEntitaetenKennzeichnenIhreIdentitaet() {
        ArchitekturRegeln.aggregateUndEntitaetenKennzeichnenIhreIdentitaet(BASIS).check(produktionsCode);
    }

    @Test
    void validierungIstEigenstaendig() {
        ArchitekturRegeln.validierungIstEigenstaendig(BASIS).check(produktionsCode);
    }

    @Test
    void portsKennenNurDieDomaene() {
        ArchitekturRegeln.portsKennenNurDieDomaene(BASIS).check(produktionsCode);
    }

    @Test
    void eingehendeUndAusgehendePortsSindGetrennt() {
        ArchitekturRegeln.eingehendeUndAusgehendePortsSindGetrennt(BASIS).check(produktionsCode);
    }

    @Test
    void serviceKenntKeineAdapter() {
        ArchitekturRegeln.serviceKenntKeineAdapter(BASIS).check(produktionsCode);
    }

    @Test
    void eingehendeAdapterKennenNurEingehendePorts() {
        ArchitekturRegeln.eingehendeAdapterKennenNurEingehendePorts(BASIS).check(produktionsCode);
    }

    @Test
    void ausgehendeAdapterKennenNurAusgehendePorts() {
        ArchitekturRegeln.ausgehendeAdapterKennenNurAusgehendePorts(BASIS).check(produktionsCode);
    }

    @Test
    void adapterHaengenNichtVomStarterAb() {
        ArchitekturRegeln.adapterHaengenNichtVomStarterAb(BASIS).check(produktionsCode);
    }

    @Test
    void domaenenmodellKenntKeineDomaenenservices() {
        ArchitekturRegeln.domaenenmodellKenntKeineDomaenenservices(BASIS).check(produktionsCode);
    }

    @Test
    void nurDieAnwendungsfaelleKennenDomaenenservices() {
        ArchitekturRegeln.nurDieAnwendungsfaelleKennenDomaenenservices(BASIS).check(produktionsCode);
    }

    @Test
    void spiIstEigenstaendig() {
        ArchitekturRegeln.spiIstEigenstaendig(BASIS).check(produktionsCode);
    }

    @Test
    void kernKenntDasSpiNicht() {
        ArchitekturRegeln.kernKenntDasSpiNicht(BASIS).check(produktionsCode);
    }

    @Test
    void nurAdapterHostUndStarterKennenDasSpi() {
        ArchitekturRegeln.nurAdapterHostUndStarterKennenDasSpi(BASIS).check(produktionsCode);
    }

    @Test
    void apiIstEigenstaendig() {
        ArchitekturRegeln.apiIstEigenstaendig(BASIS).check(produktionsCode);
    }

    @Test
    void nurAdapterApiUndStarterKennenDieApi() {
        ArchitekturRegeln.nurAdapterApiUndStarterKennenDieApi(BASIS).check(produktionsCode);
    }

    @Test
    void adapterSindVoneinanderIsoliert() {
        ArchitekturRegeln.adapterSindVoneinanderIsoliert(BASIS).check(produktionsCode);
    }
}
