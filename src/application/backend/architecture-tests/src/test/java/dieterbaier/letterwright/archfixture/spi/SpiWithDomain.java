package dieterbaier.letterwright.archfixture.spi;

import dieterbaier.letterwright.archfixture.domain.model.ModelWithDomainService;

/** Fixture: deliberate violation, the SPI exposes a type of the domain model. */
public interface SpiWithDomain {

    ModelWithDomainService model();
}
