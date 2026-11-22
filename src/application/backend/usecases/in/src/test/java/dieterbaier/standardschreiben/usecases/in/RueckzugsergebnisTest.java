package dieterbaier.standardschreiben.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class RueckzugsergebnisTest {

    private static final Versionsverweis VERSION = new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE);

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void jedesErgebnisIstUnterscheidbar() {
        assertThat(Stream.of(new Rueckzugsergebnis.Zurueckgezogen(VERSION), new Rueckzugsergebnis.NichtErlaubt(),
                new Rueckzugsergebnis.VorlageUnbekannt(), new Rueckzugsergebnis.NichtsVeroeffentlicht(),
                new Rueckzugsergebnis.Veraltet())
                .map(ergebnis -> switch (ergebnis) {
                    case Rueckzugsergebnis.Zurueckgezogen zurueckgezogen -> 200;
                    case Rueckzugsergebnis.NichtErlaubt nichtErlaubt -> 403;
                    case Rueckzugsergebnis.VorlageUnbekannt unbekannt -> 404;
                    case Rueckzugsergebnis.NichtsVeroeffentlicht nichts -> 422;
                    case Rueckzugsergebnis.Veraltet veraltet -> 409;
                })).containsExactly(200, 403, 404, 422, 409);
    }

    @Test
    void zurueckgezogenNenntDieVersion() {
        assertThat(new Rueckzugsergebnis.Zurueckgezogen(VERSION).version()).isEqualTo(VERSION);
        assertThatThrownBy(() -> new Rueckzugsergebnis.Zurueckgezogen(null))
                .hasMessageContaining("zurückgezogene Version");
    }
}
