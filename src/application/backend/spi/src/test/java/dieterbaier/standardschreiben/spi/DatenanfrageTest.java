package dieterbaier.standardschreiben.spi;

import static dieterbaier.standardschreiben.spi.Pruefungen.lehntAb;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DatenanfrageTest {

    private static final FachlicherBezug KUNDE = new FachlicherBezug("Kunde", "4711");
    private static final Standardschreibenkennung MAHNUNG =
            new Standardschreibenkennung(UUID.randomUUID(), "Mahnung");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void dieBenutzerkennungLehntFehlendeLeereUndBlankeWerteAb(String wert) {
        lehntAb(() -> new Datenanfrage(wert, KUNDE, MAHNUNG), "Die Benutzerkennung");
    }

    @Test
    void bezugUndStandardschreibenSindPflicht() {
        lehntAb(() -> new Datenanfrage("erika", null, MAHNUNG), "Der fachliche Bezug");
        lehntAb(() -> new Datenanfrage("erika", KUNDE, null), "Das Standardschreiben");
    }
}
