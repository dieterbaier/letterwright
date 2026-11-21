package dieterbaier.standardschreiben.domaene.modell;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DatenkontextTest {

    private static final Map<String, Object> KUNDE = Map.of(
            "empfaenger", Map.of("name", "Erika Mustermann", "ort", "Köln"),
            "posten", List.of("Beitrag 2026", "Mahngebühr"));

    @Test
    void gleicherInhaltErgibtGleichePruefsummeUnabhaengigVonDerSchluesselreihenfolge() {
        Map<String, Object> vorwaerts = new LinkedHashMap<>();
        vorwaerts.put("a", "1");
        vorwaerts.put("b", "2");
        Map<String, Object> rueckwaerts = new LinkedHashMap<>();
        rueckwaerts.put("b", "2");
        rueckwaerts.put("a", "1");

        assertThat(Datenkontext.aus(vorwaerts).pruefsumme()).isEqualTo(Datenkontext.aus(rueckwaerts).pruefsumme());
        assertThat(Datenkontext.aus(vorwaerts)).isEqualTo(Datenkontext.aus(rueckwaerts))
                .hasSameHashCodeAs(Datenkontext.aus(rueckwaerts));
    }

    @Test
    void jedeAenderungEinesWertsAendertDiePruefsumme() {
        Pruefsumme vorher = Datenkontext.aus(KUNDE).pruefsumme();

        Map<String, Object> anderer = new HashMap<>(KUNDE);
        anderer.put("empfaenger", Map.of("name", "Erika Mustermann", "ort", "Bonn"));

        assertThat(Datenkontext.aus(anderer).pruefsumme()).isNotEqualTo(vorher);
    }

    @Test
    void dieReihenfolgeInListenZaehlt() {
        Datenkontext ab = Datenkontext.aus(Map.of("posten", List.of("a", "b")));
        Datenkontext ba = Datenkontext.aus(Map.of("posten", List.of("b", "a")));

        assertThat(ab.pruefsumme()).isNotEqualTo(ba.pruefsumme());
        assertThat(ab).isNotEqualTo(ba);
    }

    @Test
    void dieKodierungIstEindeutigAuchWennTexteWieStrukturAussehen() {
        // Ohne Längen und Typkennungen ergäben diese Paare dieselbe Bytefolge.
        assertThat(Datenkontext.aus(Map.of("a", "bc")).pruefsumme())
                .isNotEqualTo(Datenkontext.aus(Map.of("ab", "c")).pruefsumme());
        assertThat(Datenkontext.aus(Map.of("x", List.of("ab"))).pruefsumme())
                .isNotEqualTo(Datenkontext.aus(Map.of("x", List.of("a", "b"))).pruefsumme());
        assertThat(Datenkontext.aus(Map.of("x", "")).pruefsumme())
                .isNotEqualTo(Datenkontext.aus(Map.of("x", List.of())).pruefsumme())
                .isNotEqualTo(Datenkontext.aus(Map.of("x", Map.of())).pruefsumme());
    }

    @Test
    void diePruefsummeIstStabil() {
        // Festgeschrieben, damit eine Änderung der Kodierung auffällt: Snapshots, die vor der Änderung angelegt
        // wurden, würden sonst beim Speichern als geändert gelten. Der Wert ist unabhängig von dieser Implementierung
        // nachgerechnet (Python, hashlib): M,2 | T,"name" T,"Erika" | T,"posten" L,1 T,"a"; Längen als int32 big-endian.
        assertThat(Datenkontext.aus(Map.of("name", "Erika", "posten", List.of("a"))).pruefsumme().hex())
                .isEqualTo("8b42f1fd375ad7eb974dddf7c451c706141bf7f5b3f84254b5ea0f1de7de7f69");
    }

    @Test
    void istTiefUnveraenderlichUndUnabhaengigVonDerQuelle() {
        List<Object> posten = new ArrayList<>(List.of("a"));
        Map<String, Object> quelle = new HashMap<>(Map.of("posten", posten, "unter", new HashMap<>(Map.of("k", "v"))));
        Datenkontext kontext = Datenkontext.aus(quelle);

        posten.add("b");
        quelle.put("neu", "x");

        assertThat(kontext.werte()).containsOnlyKeys("posten", "unter");
        assertThat(kontext.werte().get("posten")).isEqualTo(List.of("a"));
        assertThatThrownBy(() -> kontext.werte().put("neu", "x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ((List<?>) kontext.werte().get("posten")).clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ((Map<?, ?>) kontext.werte().get("unter")).clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void dieWerteSindNachSchluesselSortiert() {
        Map<String, Object> quelle = new LinkedHashMap<>();
        quelle.put("b", "2");
        quelle.put("a", "1");

        assertThat(Datenkontext.aus(quelle).werte().keySet()).containsExactly("a", "b");
    }

    @Test
    void lehntAndereTypenAbUndNenntDenPfad() {
        lehntAb(() -> Datenkontext.aus(Map.of("betrag", Map.of("netto", 12))), "betrag.netto");
        lehntAb(() -> Datenkontext.aus(Map.of("betrag", Map.of("netto", 12))), "java.lang.Integer");
        lehntAb(() -> Datenkontext.aus(Map.of("posten", List.of("a", List.of(1)))), "posten[1][0]");
    }

    @Test
    void lehntFehlendeWerteAb() {
        Map<String, Object> quelle = new HashMap<>();
        quelle.put("name", null);

        lehntAb(() -> Datenkontext.aus(null), "Werte des Datenkontexts");
        lehntAb(() -> Datenkontext.aus(quelle), "name");
        lehntAb(() -> Datenkontext.aus(Map.of("posten", Arrays.asList("a", null))), "posten[1]");
    }

    @Test
    void lehntSchluesselAbDieKeinTextSind() {
        lehntAb(() -> Datenkontext.aus(Map.of("unter", Map.of(1, "x"))), "unter");
    }

    @Test
    void lehntEinzelneSurrogateInWertenAbUndNenntDenPfad() {
        // Ohne diese Prüfung kodierte UTF-8 das Surrogat als '?', und beide Kontexte hätten dieselbe Prüfsumme.
        lehntAb(() -> Datenkontext.aus(Map.of("x", "\uD800")), "x");
        lehntAb(() -> Datenkontext.aus(Map.of("posten", List.of("a\uDC00b"))), "posten[0]");
    }

    @Test
    void lehntEinzelneSurrogateInSchluesselnAb() {
        lehntAb(() -> Datenkontext.aus(Map.of("unter", Map.of("k\uD800", "x"))), "Schlüssel unter.k");
    }

    @Test
    void gueltigeSurrogatpaareSindErlaubtUndUnterscheidbar() {
        Datenkontext emoji = Datenkontext.aus(Map.of("gruss", "Hallo \uD83D\uDE00"));
        Datenkontext fragezeichen = Datenkontext.aus(Map.of("gruss", "Hallo ?"));

        assertThat(emoji.werte()).containsEntry("gruss", "Hallo 😀");
        assertThat(emoji.pruefsumme()).isNotEqualTo(fragezeichen.pruefsumme());
    }

    @Test
    void toStringZeigtKeineWerte() {
        assertThat(Datenkontext.aus(KUNDE).toString()).doesNotContain("Erika").contains("pruefsumme=");
    }

    @Test
    void istNurMitDatenkontextenGleich() {
        assertThat(Datenkontext.aus(KUNDE)).isNotEqualTo(KUNDE);
    }
}
