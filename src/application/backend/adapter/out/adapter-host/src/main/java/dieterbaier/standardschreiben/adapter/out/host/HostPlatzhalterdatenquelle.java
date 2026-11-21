package dieterbaier.standardschreiben.adapter.out.host;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Standardschreiben;
import dieterbaier.standardschreiben.spi.PlatzhalterdatenAnbieter;
import dieterbaier.standardschreiben.usecases.out.Platzhalterdatenquelle;
import dieterbaier.standardschreiben.validierung.Pruefe;

/** Fragt den {@link PlatzhalterdatenAnbieter} des Hosts und übernimmt seine Werte ins Domänenmodell. */
public final class HostPlatzhalterdatenquelle implements Platzhalterdatenquelle {

    private final PlatzhalterdatenAnbieter anbieter;

    public HostPlatzhalterdatenquelle(PlatzhalterdatenAnbieter anbieter) {
        this.anbieter = Pruefe.pflicht(anbieter, "Der PlatzhalterdatenAnbieter");
    }

    @Override
    public Datenkontext liesDatenkontext(
            BenutzerKennung benutzer, FachlicherBezug bezug, Standardschreiben standardschreiben) {
        dieterbaier.standardschreiben.spi.Datenkontext antwort =
                anbieter.datenkontext(Abbildung.datenanfrage(benutzer, bezug, standardschreiben));
        if (antwort == null) {
            throw new HostVertragVerletzt("Der PlatzhalterdatenAnbieter des Hosts hat null statt eines Datenkontexts"
                    + " geliefert");
        }
        return Abbildung.zurDomaene(antwort);
    }
}
