package dieterbaier.standardschreiben.domaene.modell.schreiben;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DokumentverweisTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void derDokumentverweisLehntFehlendeLeereUndBlankeWerteAb(String wert) {
        lehntAb(() -> new Dokumentverweis(wert), "Dokumentverweis");
    }
}
