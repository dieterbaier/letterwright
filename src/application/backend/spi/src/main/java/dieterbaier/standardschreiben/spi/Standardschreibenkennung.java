package dieterbaier.standardschreiben.spi;

import java.util.UUID;

/** Kennzeichnet das Standardschreiben, für das der Host Daten liefern soll: seine ID und sein Name. */
public record Standardschreibenkennung(UUID id, String name) {

    public Standardschreibenkennung {
        Pflicht.wert(id, "Die ID des Standardschreibens");
        Pflicht.text(name, "Der Name des Standardschreibens");
    }
}
