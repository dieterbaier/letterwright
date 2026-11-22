package dieterbaier.standardschreiben.adapter.out.host;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Loeschentscheidung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.spi.Loeschrichtlinie;
import dieterbaier.standardschreiben.usecases.out.Loeschregeln;
import org.jspecify.annotations.Nullable;

/**
 * Fragt die {@link Loeschrichtlinie} des Hosts. Der Host liegt außerhalb der Vertrauensgrenze: Hält er den Null-Vertrag
 * des SPI nicht ein, meldet der Adapter das als {@link HostVertragVerletzt}.
 */
public final class HostLoeschregeln implements Loeschregeln {

    private final Loeschrichtlinie richtlinie;

    public HostLoeschregeln(Loeschrichtlinie richtlinie) {
        this.richtlinie = richtlinie;
    }

    @Override
    public Loeschentscheidung entscheide(BenutzerKennung benutzer, Schreiben schreiben) {
        dieterbaier.standardschreiben.spi.@Nullable Loeschentscheidung antwort =
                richtlinie.entscheide(Abbildung.loeschanfrage(benutzer, schreiben));
        if (antwort == null) {
            throw new HostVertragVerletzt("Die Loeschrichtlinie des Hosts hat null statt einer Entscheidung geliefert");
        }
        return Abbildung.zurDomaene(antwort);
    }
}
