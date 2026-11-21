package dieterbaier.standardschreiben.domaene.modell.schreiben;

import dieterbaier.standardschreiben.validierung.Pruefe;
import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Identität eines gespeicherten Schreibens.
 *
 * <p>Der Konstruktor stellt eine bestehende Identität wieder her, {@link #neu()} vergibt eine neue.
 */
@ValueObject
public record SchreibenId(UUID wert) {

    public SchreibenId {
        Pruefe.pflicht(wert, "Der Wert der SchreibenId");
    }

    /** Vergibt eine neue, zufällige Identität. */
    public static SchreibenId neu() {
        return new SchreibenId(UUID.randomUUID());
    }
}
