package dieterbaier.standardschreiben.domaene.modell.vorlage;

import org.jmolecules.ddd.annotation.ValueObject;

/** Zustand einer Vorlagenversion (ADR-007). */
@ValueObject
public enum Vorlagenzustand {

    /** In Bearbeitung, änderbar, für Sachbearbeiter nicht sichtbar. Je Vorlage gibt es höchstens einen. */
    ENTWURF,

    /** Freigegeben und unveränderlich. Neue Schreiben entstehen mit ihr; je Vorlage gibt es höchstens eine (ADR-022). */
    VEROEFFENTLICHT,

    /**
     * Durch eine neuere veröffentlichte Version ersetzt. Nicht mehr für neue Schreiben verwendbar; bestehende Schreiben
     * verweisen weiter darauf.
     */
    ABGELOEST,

    /**
     * Ohne Nachfolger zurückgenommen. Nicht mehr für neue Schreiben verwendbar; bestehende Schreiben verweisen weiter
     * darauf.
     */
    ZURUECKGEZOGEN
}
