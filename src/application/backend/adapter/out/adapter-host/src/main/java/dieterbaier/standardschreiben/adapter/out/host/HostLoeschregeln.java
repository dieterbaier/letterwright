package dieterbaier.standardschreiben.adapter.out.host;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Loeschentscheidung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.spi.Loeschrichtlinie;
import dieterbaier.standardschreiben.usecases.out.Loeschregeln;
import dieterbaier.standardschreiben.validierung.Pruefe;

/** Fragt die {@link Loeschrichtlinie} des Hosts. */
public final class HostLoeschregeln implements Loeschregeln {

    private final Loeschrichtlinie richtlinie;

    public HostLoeschregeln(Loeschrichtlinie richtlinie) {
        this.richtlinie = Pruefe.pflicht(richtlinie, "Die Loeschrichtlinie");
    }

    @Override
    public Loeschentscheidung entscheide(BenutzerKennung benutzer, Schreiben schreiben) {
        dieterbaier.standardschreiben.spi.Loeschentscheidung antwort = richtlinie.entscheide(Abbildung.loeschanfrage(
                Pruefe.pflicht(benutzer, "Der Benutzer"), Pruefe.pflicht(schreiben, "Das Schreiben")));
        if (antwort == null) {
            throw new HostVertragVerletzt("Die Loeschrichtlinie des Hosts hat null statt einer Entscheidung geliefert");
        }
        return Abbildung.zurDomaene(antwort);
    }
}
