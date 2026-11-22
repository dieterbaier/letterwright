package dieterbaier.standardschreiben.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class VeroeffentlichungsergebnisTest {

    private static final Versionsverweis VERSION = new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE);

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void jedesErgebnisIstUnterscheidbar() {
        assertThat(Stream.of(new Veroeffentlichungsergebnis.Veroeffentlicht(VERSION),
                new Veroeffentlichungsergebnis.NichtKonform(List.of("Befund")),
                new Veroeffentlichungsergebnis.NichtErlaubt(), new Veroeffentlichungsergebnis.VorlageUnbekannt(),
                new Veroeffentlichungsergebnis.KeinEntwurf(), new Veroeffentlichungsergebnis.EntwurfOhneInhalt(),
                new Veroeffentlichungsergebnis.BriefpapierNichtVeroeffentlicht(),
                new Veroeffentlichungsergebnis.Veraltet())
                .map(ergebnis -> switch (ergebnis) {
                    case Veroeffentlichungsergebnis.Veroeffentlicht veroeffentlicht -> "veröffentlicht";
                    case Veroeffentlichungsergebnis.NichtKonform nichtKonform -> "nicht konform";
                    case Veroeffentlichungsergebnis.NichtErlaubt nichtErlaubt -> "nicht erlaubt";
                    case Veroeffentlichungsergebnis.VorlageUnbekannt unbekannt -> "unbekannt";
                    case Veroeffentlichungsergebnis.KeinEntwurf keinEntwurf -> "kein Entwurf";
                    case Veroeffentlichungsergebnis.EntwurfOhneInhalt ohneInhalt -> "ohne Inhalt";
                    case Veroeffentlichungsergebnis.BriefpapierNichtVeroeffentlicht briefpapier -> "Briefpapier fehlt";
                    case Veroeffentlichungsergebnis.Veraltet veraltet -> "veraltet";
                })).doesNotHaveDuplicates().hasSize(8);
    }

    @Test
    void veroeffentlichtNenntDieVersion() {
        assertThat(new Veroeffentlichungsergebnis.Veroeffentlicht(VERSION).version()).isEqualTo(VERSION);
        assertThatThrownBy(() -> new Veroeffentlichungsergebnis.Veroeffentlicht(null))
                .hasMessageContaining("veröffentlichte Version");
    }

    @Test
    void nichtKonformBrauchtMindestensEinenBefundUndKopiertDieListe() {
        List<String> befunde = new ArrayList<>(List.of("Bild ohne Alternativtext"));
        var nichtKonform = new Veroeffentlichungsergebnis.NichtKonform(befunde);
        befunde.add("später");

        assertThat(nichtKonform.befunde()).containsExactly("Bild ohne Alternativtext");
        assertThatThrownBy(() -> new Veroeffentlichungsergebnis.NichtKonform(List.of()))
                .hasMessageContaining("mindestens einen Befund");
        assertThatThrownBy(() -> new Veroeffentlichungsergebnis.NichtKonform(Arrays.asList("a", null)))
                .hasMessageContaining("Befunde");
    }
}
