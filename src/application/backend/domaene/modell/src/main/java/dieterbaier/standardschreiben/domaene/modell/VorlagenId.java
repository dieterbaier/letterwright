package dieterbaier.standardschreiben.domaene.modell;

import java.util.UUID;

/** Identität eines Briefpapiers oder Standardschreibens über alle Versionen hinweg. */
public record VorlagenId(UUID wert) {

    /** Erzeugt eine neue, zufällige Identität. */
    public static VorlagenId neu() {
        return new VorlagenId(UUID.randomUUID());
    }
}
