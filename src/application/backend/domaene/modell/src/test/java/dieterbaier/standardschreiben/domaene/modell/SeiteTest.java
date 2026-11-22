package dieterbaier.standardschreiben.domaene.modell;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class SeiteTest {

    private final Seitenanfrage zweiteSeite = new Seitenanfrage(1, 2);

    @Test
    void dieSeiteZaehltDieSeitenAusDerGesamtzahl() {
        assertThat(new Seite<>(List.of("c", "d"), 5, zweiteSeite).anzahlSeiten()).isEqualTo(3);
        assertThat(new Seite<>(List.of("c", "d"), 4, zweiteSeite).anzahlSeiten()).isEqualTo(2);
        assertThat(new Seite<>(List.of(), 0, new Seitenanfrage(0, 2)).anzahlSeiten()).isZero();
        assertThat(new Seite<>(List.of(), 2, zweiteSeite).anzahlSeiten()).as("direkt hinter dem Ende").isEqualTo(1);
        assertThat(new Seite<>(List.of(), 3, new Seitenanfrage(2, 2)).anzahlSeiten())
                .as("weiter hinter dem Ende").isEqualTo(2);
    }

    @Test
    void dieSeiteHaeltEineUnveraenderlicheKopie() {
        List<String> eintraege = new ArrayList<>(List.of("c"));
        Seite<String> seite = new Seite<>(eintraege, 3, zweiteSeite);

        eintraege.add("d");

        assertThat(seite.eintraege()).containsExactly("c");
    }

    @Test
    void dieSeitePasstZurAnfrageUndZurGesamtzahl() {
        lehntAb(() -> new Seite<>(null, 0, zweiteSeite), "Einträge der Seite");
        lehntAb(() -> new Seite<>(Arrays.asList("c", null), 4, zweiteSeite), "Einträge der Seite[1]");
        lehntAb(() -> new Seite<>(List.of("c"), 3, null), "Seitenanfrage");
        lehntAb(() -> new Seite<>(List.of("c", "d", "e"), 5, zweiteSeite), "höchstens 2");
        lehntAb(() -> new Seite<>(List.of("c", "d"), 3, zweiteSeite), "Gesamtzahl 3");
        lehntAb(() -> new Seite<>(List.of("e"), 3, new Seitenanfrage(3, 2)), "Gesamtzahl 3");
        lehntAb(() -> new Seite<>(List.of(), -1, zweiteSeite), "nicht negativ");
    }
}
