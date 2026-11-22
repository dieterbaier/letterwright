package dieterbaier.standardschreiben.domaene.modell.schreiben;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class SchreibenzustandTest {

    @Test
    void eineLogischeLoeschungKenntIhrenZeitpunkt() {
        Instant am = Instant.parse("2026-11-22T11:00:00Z");

        assertThat(new Schreibenzustand.LogischGeloescht(am).am()).isEqualTo(am);
        lehntAb(() -> new Schreibenzustand.LogischGeloescht(null), "Zeitpunkt des Löschens");
    }

    @Test
    void zustaendeSindWerte() {
        Instant am = Instant.parse("2026-11-22T11:00:00Z");

        assertThat(new Schreibenzustand.Sichtbar()).isEqualTo(new Schreibenzustand.Sichtbar());
        assertThat(new Schreibenzustand.LogischGeloescht(am)).isEqualTo(new Schreibenzustand.LogischGeloescht(am))
                .isNotEqualTo(new Schreibenzustand.Sichtbar());
    }
}
