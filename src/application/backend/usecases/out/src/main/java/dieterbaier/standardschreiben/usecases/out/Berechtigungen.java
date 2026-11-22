package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;

/** Fragt den Host, ob ein Benutzer eine Aktion ausführen darf. Implementiert von {@code adapter-host} (ADR-016). */
public interface Berechtigungen {

    boolean darf(BenutzerKennung benutzer, VorlagenAktion aktion);

    boolean darf(BenutzerKennung benutzer, SchreibenAktion aktion, FachlicherBezug bezug);

    boolean darfErzeugen(BenutzerKennung benutzer, FachlicherBezug bezug, Standardschreiben standardschreiben);
}
