package dieterbaier.standardschreiben.spi;

import static dieterbaier.standardschreiben.spi.Pruefungen.lehntAb;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class LoeschanfrageTest {

    private static final FachlicherBezug KUNDE = new FachlicherBezug("Kunde", "4711");
    private static final Instant ERSTELLT = Instant.parse("2026-11-21T10:00:00Z");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void dieBenutzerkennungLehntFehlendeLeereUndBlankeWerteAb(String wert) {
        lehntAb(() -> new Loeschanfrage(wert, UUID.randomUUID(), KUNDE, ERSTELLT), "Die Benutzerkennung");
    }

    @Test
    void schreibenBezugUndErstellungszeitpunktSindPflicht() {
        lehntAb(() -> new Loeschanfrage("erika", null, KUNDE, ERSTELLT), "Die ID des Schreibens");
        lehntAb(() -> new Loeschanfrage("erika", UUID.randomUUID(), null, ERSTELLT), "Der fachliche Bezug");
        lehntAb(() -> new Loeschanfrage("erika", UUID.randomUUID(), KUNDE, null), "Der Erstellungszeitpunkt");
    }
}
