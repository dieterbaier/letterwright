package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;

/** Liefert den Datenkontext des Hosts in Domänenbegriffen. Implementiert von {@code adapter-host} (ADR-016). */
public interface Platzhalterdatenquelle {

    /**
     * Liest die bereits formatierten Werte für ein Standardschreiben und einen fachlichen Bezug.
     *
     * @throws IllegalStateException wenn der Host keinen gültigen Datenkontext liefert
     */
    Datenkontext liesDatenkontext(BenutzerKennung benutzer, FachlicherBezug bezug, Standardschreiben standardschreiben);
}
