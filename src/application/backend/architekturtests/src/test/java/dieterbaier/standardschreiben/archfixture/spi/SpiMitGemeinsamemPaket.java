package dieterbaier.standardschreiben.archfixture.spi;

import dieterbaier.standardschreiben.archfixture.gemeinsam.GemeinsameHilfsklasse;

/** Fixture: absichtlicher Verstoß, das SPI nutzt ein Paket der Bibliothek außerhalb von {@code spi}. */
public interface SpiMitGemeinsamemPaket {

    default String bezug(String wert) {
        return GemeinsameHilfsklasse.normalisiere(wert);
    }
}
