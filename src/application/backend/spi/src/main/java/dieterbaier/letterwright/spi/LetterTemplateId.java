package dieterbaier.letterwright.spi;

import java.util.UUID;

/** Kennzeichnet das Standardschreiben, für das der Host Daten liefern soll: seine ID und sein Name. */
public record LetterTemplateId(UUID id, String name) {

    public LetterTemplateId {
        Require.value(id, "Die ID des Standardschreibens");
        Require.text(name, "Der Name des Standardschreibens");
    }
}
