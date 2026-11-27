package dieterbaier.letterwright.archfixture.spi;

import dieterbaier.letterwright.archfixture.shared.SharedHelper;

/** Fixture: absichtlicher Verstoß, das SPI nutzt ein Paket der Bibliothek außerhalb von {@code spi}. */
public interface SpiWithSharedPackage {

    default String reference(String value) {
        return SharedHelper.normalize(value);
    }
}
