package dieterbaier.standardschreiben.domaene.modell.vorlage;

import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Verweist von außen auf genau eine Version eines Briefpapiers oder Standardschreibens. Snapshot und Schreiben halten
 * nur diesen Verweis, nie die Version selbst; sie gehört zum Aggregat ihrer Vorlage.
 */
@ValueObject
public record Versionsverweis(VorlagenId vorlage, Versionsnummer nummer) {

    public Versionsverweis {
        Pruefe.pflicht(vorlage, "Die Vorlage des Versionsverweises");
        Pruefe.pflicht(nummer, "Die Nummer des Versionsverweises");
    }
}
