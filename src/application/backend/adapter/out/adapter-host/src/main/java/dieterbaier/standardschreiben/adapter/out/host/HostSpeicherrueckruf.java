package dieterbaier.standardschreiben.adapter.out.host;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Rueckrufergebnis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.spi.NachDemSpeichern;
import dieterbaier.standardschreiben.usecases.out.Speicherrueckruf;
import org.jspecify.annotations.Nullable;

/**
 * Ruft den optionalen Rückruf {@link NachDemSpeichern} des Hosts (ADR-023). Der Host liegt außerhalb der
 * Vertrauensgrenze: Hält er den Null-Vertrag des SPI nicht ein, meldet der Adapter das als {@link HostVertragVerletzt}.
 * Eine Ausnahme des Hosts reicht er unverändert durch; sie ist ein technischer Fehler.
 */
public final class HostSpeicherrueckruf implements Speicherrueckruf {

    private final NachDemSpeichern rueckruf;

    private HostSpeicherrueckruf(NachDemSpeichern rueckruf) {
        this.rueckruf = rueckruf;
    }

    /** Für einen Host, der den Rückruf bereitstellt. */
    public static Speicherrueckruf zu(NachDemSpeichern rueckruf) {
        return new HostSpeicherrueckruf(rueckruf);
    }

    /** Für einen Host ohne Rückruf und für die Java-API: Jedes Schreiben ist angenommen, gefragt wird niemand. */
    public static Speicherrueckruf ohne() {
        return (benutzer, schreiben) -> Rueckrufergebnis.angenommen();
    }

    @Override
    public Rueckrufergebnis angelegt(BenutzerKennung benutzer, Schreiben schreiben) {
        dieterbaier.standardschreiben.spi.@Nullable Rueckrufergebnis antwort =
                rueckruf.angelegt(Abbildung.gespeichertesSchreiben(benutzer, schreiben));
        if (antwort == null) {
            throw new HostVertragVerletzt("Der Rückruf NachDemSpeichern des Hosts hat null statt eines Ergebnisses"
                    + " geliefert");
        }
        return Abbildung.zurDomaene(antwort);
    }
}
