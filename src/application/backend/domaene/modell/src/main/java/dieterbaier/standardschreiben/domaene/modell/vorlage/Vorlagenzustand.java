package dieterbaier.standardschreiben.domaene.modell.vorlage;

import org.jmolecules.ddd.annotation.ValueObject;

/** Zustand einer Vorlagenversion (ADR-007). */
@ValueObject
public enum Vorlagenzustand {

    /** In Bearbeitung, änderbar, für Sachbearbeiter nicht sichtbar. Je Vorlage gibt es höchstens einen. */
    ENTWURF,

    /** Freigegeben und unveränderlich. */
    VEROEFFENTLICHT,

    /** Nicht mehr für neue Schreiben verwendbar. Bestehende Schreiben verweisen weiter darauf. */
    ZURUECKGEZOGEN
}
