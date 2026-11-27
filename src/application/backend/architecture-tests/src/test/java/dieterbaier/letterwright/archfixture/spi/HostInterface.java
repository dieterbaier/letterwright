package dieterbaier.letterwright.archfixture.spi;

/** Fixture: eine regelkonforme SPI-Schnittstelle mit eigenem Datentyp. */
public interface HostInterface {

    HostRecord data(String reference);
}
