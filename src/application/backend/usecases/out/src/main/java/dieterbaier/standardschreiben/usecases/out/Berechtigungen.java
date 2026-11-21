package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.Aktion;
import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;

/** Fragt den Host, ob ein Benutzer eine Aktion ausführen darf. Implementiert von {@code adapter-host} (ADR-016). */
public interface Berechtigungen {

    /**
     * Ob der Benutzer die Aktion für den fachlichen Bezug ausführen darf.
     *
     * @throws IllegalArgumentException wenn die Aktion sich auf keinen Bezug bezieht
     */
    boolean darf(BenutzerKennung benutzer, Aktion aktion, FachlicherBezug bezug);

    /**
     * Ob der Benutzer eine Aktion ohne fachlichen Bezug ausführen darf, also eine Aktion für Vorlagen.
     *
     * @throws IllegalArgumentException wenn die Aktion sich auf einen Bezug bezieht
     */
    boolean darf(BenutzerKennung benutzer, Aktion aktion);
}
