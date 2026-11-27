package dieterbaier.letterwright.domain.model.template;

import org.jmolecules.ddd.annotation.ValueObject;

/** Zustand einer Vorlagenversion (ADR-007). */
@ValueObject
public enum TemplateVersionState {

    /** In Bearbeitung, änderbar, für Sachbearbeiter nicht sichtbar. Je Vorlage gibt es höchstens einen. */
    DRAFT,

    /** Freigegeben und unveränderlich. Neue Schreiben entstehen mit ihr; je Vorlage gibt es höchstens eine (ADR-022). */
    PUBLISHED,

    /**
     * Durch eine neuere veröffentlichte Version ersetzt. Nicht mehr für neue Schreiben verwendbar; bestehende Schreiben
     * verweisen weiter darauf.
     */
    SUPERSEDED,

    /**
     * Ohne Nachfolger zurückgenommen. Nicht mehr für neue Schreiben verwendbar; bestehende Schreiben verweisen weiter
     * darauf.
     */
    WITHDRAWN
}
