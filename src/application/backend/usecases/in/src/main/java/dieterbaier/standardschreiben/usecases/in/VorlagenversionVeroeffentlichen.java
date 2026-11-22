package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;

/**
 * Anwendungsfall „Vorlagenversion veröffentlichen“: Der Entwurf eines Briefpapiers oder Standardschreibens wird zur
 * einzigen veröffentlichten Version, wenn sein Probe-PDF mit den Beispieldaten konform ist (ADR-005, ADR-022). Die bisher
 * veröffentlichte Version ist danach abgelöst.
 */
public interface VorlagenversionVeroeffentlichen {

    Veroeffentlichungsergebnis veroeffentliche(BenutzerKennung benutzer, VorlagenId vorlage);
}
