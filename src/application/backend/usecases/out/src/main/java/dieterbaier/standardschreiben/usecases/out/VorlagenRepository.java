package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.vorlage.Briefpapier;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.util.Optional;

/** Lädt Briefpapiere und Standardschreiben mit all ihren Versionen. */
public interface VorlagenRepository {

    Optional<Standardschreiben> standardschreiben(VorlagenId id);

    Optional<Briefpapier> briefpapier(VorlagenId id);
}
