package dieterbaier.letterwright.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;

/** Eine Handlung an Briefpapier und Standardschreiben, ohne fachlichen Bezug. */
@ValueObject
public enum TemplateAction {

    /** Umfasst Anlegen, Entwürfe ändern und neue Entwürfe anlegen. */
    EDIT,

    /**
     * Umfasst auch das Zurückziehen veröffentlichter Versionen. Getrennt vom Bearbeiten, damit der Host ein Vier-Augen-Prinzip
     * abbilden kann: Was veröffentlicht ist, geht in alle neuen Schreiben ein.
     */
    PUBLISH
}
