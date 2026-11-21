package dieterbaier.standardschreiben.domaene.modell;

/** Zustand einer Vorlagenversion (ADR-007). */
public enum Vorlagenzustand {

    /** In Bearbeitung, änderbar, für Sachbearbeiter nicht sichtbar. */
    ENTWURF,

    /** Freigegeben und unveränderlich. Nur veröffentlichte Versionen werden für neue Schreiben verwendet. */
    VEROEFFENTLICHT,

    /** Nicht mehr für neue Schreiben verwendbar. Bestehende Schreiben verweisen weiter darauf. */
    ZURUECKGEZOGEN
}
