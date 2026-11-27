package dieterbaier.letterwright.archfixture.spi;

import dieterbaier.letterwright.archfixture.domain.model.ModelWithDomainService;

/** Fixture: absichtlicher Verstoß, das SPI gibt einen Typ des Domänenmodells heraus. */
public interface SpiWithDomain {

    ModelWithDomainService model();
}
