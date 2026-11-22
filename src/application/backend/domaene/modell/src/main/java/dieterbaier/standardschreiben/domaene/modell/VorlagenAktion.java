package dieterbaier.standardschreiben.domaene.modell;

import org.jmolecules.ddd.annotation.ValueObject;

/** Eine Handlung an Briefpapier und Standardschreiben, ohne fachlichen Bezug. */
@ValueObject
public enum VorlagenAktion {

    /** Umfasst Anlegen, Entwürfe ändern und neue Entwürfe anlegen. */
    BEARBEITEN,

    /**
     * Umfasst auch das Zurückziehen veröffentlichter Versionen. Getrennt vom Bearbeiten, damit der Host ein Vier-Augen-Prinzip
     * abbilden kann: Was veröffentlicht ist, geht in alle neuen Schreiben ein.
     */
    VEROEFFENTLICHEN
}
