package dieterbaier.letterwright.archfixture.spi;

import dieterbaier.letterwright.archfixture.shared.SharedHelper;

/** Fixture: deliberate violation, the SPI uses a package of the library outside {@code spi}. */
public interface SpiWithSharedPackage {

    default String reference(String value) {
        return SharedHelper.normalize(value);
    }
}
