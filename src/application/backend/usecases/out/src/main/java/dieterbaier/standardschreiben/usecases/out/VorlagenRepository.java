package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.vorlage.Briefpapier;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.util.Optional;

/** Lädt und ersetzt Briefpapiere und Standardschreiben mit all ihren Versionen. */
public interface VorlagenRepository {

    Optional<Standardschreiben> standardschreiben(VorlagenId id);

    Optional<Briefpapier> briefpapier(VorlagenId id);

    /**
     * Ersetzt das Standardschreiben durch {@code geaendert}, in einem Schritt und nur, wenn der gespeicherte Stand mit allen
     * Versionen, Zuständen, Inhalten und Beispieldaten noch {@code geladen} entspricht. Sonst ersetzt es nichts. So wird
     * nie ein Inhalt veröffentlicht, der nicht geprüft wurde (ADR-022).
     *
     * <p>Eine Ausnahme heißt: Ob ersetzt wurde, ist ungewiss.
     */
    Ersetzungsausgang ersetze(Standardschreiben geladen, Standardschreiben geaendert);

    /** Wie {@link #ersetze(Standardschreiben, Standardschreiben)}, für ein Briefpapier. */
    Ersetzungsausgang ersetze(Briefpapier geladen, Briefpapier geaendert);
}
