package dieterbaier.standardschreiben.domaene.modell;

import dieterbaier.standardschreiben.validierung.Pruefe;
import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Identität eines Briefpapiers oder Standardschreibens über alle Versionen hinweg.
 *
 * <p>Der Konstruktor stellt eine bestehende Identität wieder her, {@link #neu()} vergibt eine neue.
 */
@ValueObject
public record VorlagenId(UUID wert) {

    public VorlagenId {
        Pruefe.pflicht(wert, "Der Wert der VorlagenId");
    }

    /** Vergibt eine neue, zufällige Identität. */
    public static VorlagenId neu() {
        return new VorlagenId(UUID.randomUUID());
    }
}
