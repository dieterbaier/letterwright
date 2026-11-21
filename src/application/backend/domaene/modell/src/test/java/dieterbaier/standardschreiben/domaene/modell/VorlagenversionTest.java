package dieterbaier.standardschreiben.domaene.modell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class VorlagenversionTest {

    private final VorlagenId vorlage = VorlagenId.neu();
    private final Vorlagenversion entwurf = Vorlagenversion.ersterEntwurf(vorlage, "<p>Hallo</p>");

    @Test
    void ersteVersionIstEinEntwurfMitNummerEins() {
        assertThat(entwurf.zustand()).isEqualTo(Vorlagenzustand.ENTWURF);
        assertThat(entwurf.verweis()).isEqualTo(new Versionsverweis(vorlage, Versionsnummer.ERSTE));
    }

    @Test
    void einEntwurfIstAenderbar() {
        Vorlagenversion geaendert = entwurf.mitInhalt("<p>Guten Tag</p>");

        assertThat(geaendert.inhalt()).isEqualTo("<p>Guten Tag</p>");
        assertThat(geaendert.verweis()).isEqualTo(entwurf.verweis());
        assertThat(entwurf.inhalt()).as("die Ausgangsinstanz bleibt unverändert").isEqualTo("<p>Hallo</p>");
    }

    @Test
    void lebenszyklusEntwurfVeroeffentlichtZurueckgezogen() {
        Vorlagenversion veroeffentlicht = entwurf.veroeffentlichen();
        Vorlagenversion zurueckgezogen = veroeffentlicht.zurueckziehen();

        assertThat(veroeffentlicht.zustand()).isEqualTo(Vorlagenzustand.VEROEFFENTLICHT);
        assertThat(zurueckgezogen.zustand()).isEqualTo(Vorlagenzustand.ZURUECKGEZOGEN);
        assertThat(zurueckgezogen.inhalt()).isEqualTo(entwurf.inhalt());
        assertThat(zurueckgezogen.verweis()).isEqualTo(entwurf.verweis());
    }

    @Test
    void eineVeroeffentlichteVersionIstUnveraenderlich() {
        Vorlagenversion veroeffentlicht = entwurf.veroeffentlichen();

        assertThatThrownBy(() -> veroeffentlicht.mitInhalt("<p>anders</p>"))
                .isInstanceOf(UnzulaessigerZustandswechsel.class)
                .hasMessageContaining("VEROEFFENTLICHT")
                .hasMessageContaining("Ändern");
    }

    @Test
    void eineAenderungEinerVeroeffentlichtenVersionIstDerNaechsteEntwurf() {
        Vorlagenversion naechste = entwurf.veroeffentlichen().neuerEntwurf("<p>neu</p>");

        assertThat(naechste.zustand()).isEqualTo(Vorlagenzustand.ENTWURF);
        assertThat(naechste.nummer()).isEqualTo(new Versionsnummer(2));
        assertThat(naechste.vorlage()).isEqualTo(vorlage);
        assertThat(naechste.inhalt()).isEqualTo("<p>neu</p>");
    }

    @Test
    void verboteneUebergaengeAusDemEntwurf() {
        assertThatThrownBy(entwurf::zurueckziehen).isInstanceOf(UnzulaessigerZustandswechsel.class);
        assertThatThrownBy(() -> entwurf.neuerEntwurf("x")).isInstanceOf(UnzulaessigerZustandswechsel.class);
    }

    @Test
    void verboteneUebergaengeAusVeroeffentlicht() {
        Vorlagenversion veroeffentlicht = entwurf.veroeffentlichen();

        assertThatThrownBy(veroeffentlicht::veroeffentlichen).isInstanceOf(UnzulaessigerZustandswechsel.class);
    }

    @Test
    void eineZurueckgezogeneVersionLaesstKeinenUebergangMehrZu() {
        Vorlagenversion zurueckgezogen = entwurf.veroeffentlichen().zurueckziehen();

        assertThatThrownBy(() -> zurueckgezogen.mitInhalt("x")).isInstanceOf(UnzulaessigerZustandswechsel.class);
        assertThatThrownBy(zurueckgezogen::veroeffentlichen).isInstanceOf(UnzulaessigerZustandswechsel.class);
        assertThatThrownBy(zurueckgezogen::zurueckziehen).isInstanceOf(UnzulaessigerZustandswechsel.class);
        assertThatThrownBy(() -> zurueckgezogen.neuerEntwurf("x")).isInstanceOf(UnzulaessigerZustandswechsel.class);
    }
}
