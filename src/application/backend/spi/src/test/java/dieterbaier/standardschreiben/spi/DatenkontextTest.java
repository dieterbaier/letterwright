package dieterbaier.standardschreiben.spi;

import static dieterbaier.standardschreiben.spi.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DatenkontextTest {

    @Test
    void derDatenkontextIstEineUnveraenderlicheKopie() {
        Map<String, Object> werte = new HashMap<>();
        werte.put("name", "Erika");
        Datenkontext kontext = new Datenkontext(werte);
        werte.put("betrag", "12,00 €");

        assertThat(kontext.werte()).containsOnlyKeys("name");
        assertThatThrownBy(() -> kontext.werte().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void aenderungenAnVerschachteltenSammlungenDesHostsWirkenSichNichtAus() {
        List<Object> kunden = new ArrayList<>(List.of("Erika"));
        Map<String, Object> adresse = new HashMap<>(Map.of("ort", "Berlin"));
        Datenkontext kontext = new Datenkontext(Map.of("kunden", kunden, "adresse", adresse));
        int hashVorher = kontext.hashCode();

        kunden.add("Max");
        adresse.put("ort", "Hamburg");

        assertThat(kontext.werte().get("kunden")).isEqualTo(List.of("Erika"));
        assertThat(kontext.werte().get("adresse")).isEqualTo(Map.of("ort", "Berlin"));
        assertThat(kontext.hashCode()).isEqualTo(hashVorher);
    }

    @Test
    void verschachtelteSammlungenLassenSichUeberDenAccessorNichtAendern() {
        Datenkontext kontext = new Datenkontext(Map.of("posten", List.of(new HashMap<>(Map.of("betrag", "12,00 €")))));

        List<?> posten = (List<?>) kontext.werte().get("posten");
        Map<?, ?> erster = (Map<?, ?>) posten.get(0);

        assertThatThrownBy(posten::clear).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(erster::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void ungueltigeWerteBleibenFuerDiePruefungDerBibliothekErhalten() {
        Map<String, Object> werte = new HashMap<>();
        werte.put("fehlt", null);
        werte.put("zahl", 12);

        Map<String, ?> uebernommen = new Datenkontext(werte).werte();

        assertThat(uebernommen.containsKey("fehlt")).isTrue();
        assertThat(uebernommen.get("fehlt")).isNull();
        assertThat(uebernommen.get("zahl")).isEqualTo(12);
    }

    @Test
    void toStringGibtKeineWerteAus() {
        Datenkontext kontext = new Datenkontext(
                Map.of("name", "Erika Mustermann", "konten", List.of(Map.of("iban", "DE02120300000000202051"))));

        assertThat(kontext.toString())
                .doesNotContain("Erika", "Mustermann", "DE02120300000000202051", "iban")
                .isEqualTo("Datenkontext[2 Werte]");
    }

    @Test
    void verschachtelteWerteBleibenErhalten() {
        Datenkontext kontext = new Datenkontext(Map.of("posten", List.of(Map.of("betrag", "12,00 €"))));

        assertThat(kontext.werte().get("posten")).isEqualTo(List.of(Map.of("betrag", "12,00 €")));
    }

    @Test
    void dieWerteSindPflicht() {
        lehntAb(() -> new Datenkontext(null), "Die Werte des Datenkontexts");
    }
}
