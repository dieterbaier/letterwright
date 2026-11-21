package dieterbaier.standardschreiben.architektur;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import dieterbaier.standardschreiben.archfixture.adapter.in.web.WebAdapter;
import dieterbaier.standardschreiben.archfixture.adapter.out.host.HostAdapter;
import dieterbaier.standardschreiben.archfixture.adapter.out.rendering.RenderingAdapter;
import dieterbaier.standardschreiben.archfixture.spi.HostDatensatz;
import dieterbaier.standardschreiben.archfixture.spi.HostSchnittstelle;
import dieterbaier.standardschreiben.archfixture.usecases.out.AusgehenderPort;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Belegt mit absichtlich falschen Fixture-Klassen, dass jede Regel Verstöße erkennt.
 *
 * <p>Ohne diesen Nachweis könnte eine falsch formulierte Regel dauerhaft grün sein.
 */
class ArchitekturRegelnErkennenVerstoesseTest {

    private static final String FIXTURE = "dieterbaier.standardschreiben.archfixture";

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
    void domaeneMitAdapterAbhaengigkeitVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.domaeneIstFrameworkfrei(FIXTURE), "DomaeneMitAdapter");
    }

    @Test
    void domaeneMitNichtZugelassenerBibliothekVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.domaeneNutztNurZugelasseneBibliotheken(FIXTURE), "DomaeneMitFremderBibliothek");
    }

    @Test
    void entitaetOhneIdentitaetVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.aggregateUndEntitaetenKennzeichnenIhreIdentitaet(FIXTURE),
                "EntitaetOhneIdentitaet");
    }

    @Test
    void identitaetAmFeldOderAnDerMethodeErfuelltDieRegel() {
        assertThatThrownBy(() -> ArchitekturRegeln.aggregateUndEntitaetenKennzeichnenIhreIdentitaet(FIXTURE)
                        .check(fixtures))
                .hasMessageNotContaining("AggregatMitIdentitaetAmFeld")
                .hasMessageNotContaining("EntitaetMitIdentitaetAnMethode");
    }

    @Test
    void validierungMitDomaenenabhaengigkeitVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.validierungIstEigenstaendig(FIXTURE), "ValidierungMitDomaene");
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

    @Test
    void modellMitDomaenenserviceVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.domaenenmodellKenntKeineDomaenenservices(FIXTURE), "ModellMitDomaenenservice");
    }

    @Test
    void portMitDomaenenserviceVerletztDieRegel() {
        erkenntVerstoss(
                ArchitekturRegeln.nurDieAnwendungsfaelleKennenDomaenenservices(FIXTURE),
                "AusgehenderPortMitDomaenenservice");
    }

    @Test
    void adapterMitDomaenenserviceVerletztDieRegel() {
        erkenntVerstoss(
                ArchitekturRegeln.nurDieAnwendungsfaelleKennenDomaenenservices(FIXTURE),
                "WebAdapterMitDomaenenservice");
    }

    @Test
    void spiMitDomaenenabhaengigkeitVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.spiIstEigenstaendig(FIXTURE), "SpiMitDomaene");
    }

    @Test
    void spiMitAbhaengigkeitInEinNeuesPaketDerBibliothekVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.spiIstEigenstaendig(FIXTURE), "SpiMitGemeinsamemPaket");
    }

    @Test
    void spiMitSpringAbhaengigkeitVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.spiIstEigenstaendig(FIXTURE), "SpiMitSpring");
    }

    @Test
    void spiDarfSichSelbstBenutzen() {
        JavaClasses erlaubt = new ClassFileImporter()
                .importClasses(HostSchnittstelle.class, HostDatensatz.class);

        assertThatCode(() -> ArchitekturRegeln.spiIstEigenstaendig(FIXTURE).check(erlaubt))
                .doesNotThrowAnyException();
    }

    @Test
    void portMitSpiVerletztDieRegelFuerDenKern() {
        erkenntVerstoss(ArchitekturRegeln.kernKenntDasSpiNicht(FIXTURE), "AusgehenderPortMitSpi");
    }

    @Test
    void andererAdapterMitSpiVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.nurAdapterHostUndStarterKennenDasSpi(FIXTURE), "RenderingAdapterMitSpi");
    }

    @Test
    void portMitSpiVerletztDieRegelFuerDieSpiNutzer() {
        erkenntVerstoss(ArchitekturRegeln.nurAdapterHostUndStarterKennenDasSpi(FIXTURE), "AusgehenderPortMitSpi");
    }

    @Test
    void eingehenderAdapterMitAusgehendemAdapterVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.adapterSindVoneinanderIsoliert(FIXTURE), "WebAdapterMitRenderingAdapter");
    }

    @Test
    void adapterHostMitAnderemAusgehendemAdapterVerletztDieRegel() {
        erkenntVerstoss(ArchitekturRegeln.adapterSindVoneinanderIsoliert(FIXTURE), "HostAdapterMitRenderingAdapter");
    }

    @Test
    void adapterOhneGegenseitigeAbhaengigkeitErfuellenDieRegel() {
        JavaClasses erlaubt = new ClassFileImporter()
                .importClasses(WebAdapter.class, RenderingAdapter.class, HostAdapter.class);

        assertThatCode(() -> ArchitekturRegeln.adapterSindVoneinanderIsoliert(FIXTURE).check(erlaubt))
                .doesNotThrowAnyException();
    }

    @Test
    void adapterHostDarfDasSpiBenutzen() {
        JavaClasses erlaubt = new ClassFileImporter()
                .importClasses(HostAdapter.class, HostSchnittstelle.class, AusgehenderPort.class);

        assertThatCode(() -> ArchitekturRegeln.nurAdapterHostUndStarterKennenDasSpi(FIXTURE).check(erlaubt))
                .doesNotThrowAnyException();
    }
}
