package dieterbaier.standardschreiben.archfixture.spi;

/** Fixture: eine regelkonforme SPI-Schnittstelle mit eigenem Datentyp. */
public interface HostSchnittstelle {

    HostDatensatz daten(String bezug);
}
