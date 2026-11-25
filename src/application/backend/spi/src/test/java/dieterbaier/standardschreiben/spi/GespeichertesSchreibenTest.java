package dieterbaier.standardschreiben.spi;

import static dieterbaier.standardschreiben.spi.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class GespeichertesSchreibenTest {

    private final UUID id = UUID.randomUUID();
    private final FachlicherBezug kunde = new FachlicherBezug("Kunde", "4711");
    private final Standardschreibenversion mahnung = new Standardschreibenversion(UUID.randomUUID(), 2);
    private final Instant erstellt = Instant.parse("2026-11-24T10:00:00Z");

    @Test
    void traegtDieIdentifikatorenDesSchreibens() {
        var schreiben = new GespeichertesSchreiben(id, kunde, mahnung, "erika", erstellt);

        assertThat(schreiben.id()).isEqualTo(id);
        assertThat(schreiben.bezug()).isEqualTo(kunde);
        assertThat(schreiben.standardschreiben()).isEqualTo(mahnung);
        assertThat(schreiben.benutzerkennung()).isEqualTo("erika");
        assertThat(schreiben.erstellt()).isEqualTo(erstellt);
    }

    @Test
    void lehntFehlendeWerteAb() {
        lehntAb(() -> new GespeichertesSchreiben(null, kunde, mahnung, "erika", erstellt), "Die ID des Schreibens");
        lehntAb(() -> new GespeichertesSchreiben(id, null, mahnung, "erika", erstellt), "Der fachliche Bezug");
        lehntAb(() -> new GespeichertesSchreiben(id, kunde, null, "erika", erstellt), "Das Standardschreiben");
        lehntAb(() -> new GespeichertesSchreiben(id, kunde, mahnung, "erika", null), "Der Zeitpunkt der Erstellung");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void lehntEineFehlendeOderBlankeBenutzerkennungAb(String benutzerkennung) {
        lehntAb(() -> new GespeichertesSchreiben(id, kunde, mahnung, benutzerkennung, erstellt), "Die Benutzerkennung");
    }
}
