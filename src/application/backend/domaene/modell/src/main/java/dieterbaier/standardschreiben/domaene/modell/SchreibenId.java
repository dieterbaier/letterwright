package dieterbaier.standardschreiben.domaene.modell;

import java.util.UUID;

/** Identität eines gespeicherten Schreibens. */
public record SchreibenId(UUID wert) {

    /** Erzeugt eine neue, zufällige Identität. */
    public static SchreibenId neu() {
        return new SchreibenId(UUID.randomUUID());
    }
}
