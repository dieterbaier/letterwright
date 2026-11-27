package dieterbaier.letterwright.spi;

/** Eine Handlung an Briefpapier und Standardschreiben. Sie hat keinen fachlichen Bezug. */
public enum TemplateAction {

    /** Briefpapier und Standardschreiben anlegen, Entwürfe ändern und neue Entwürfe anlegen. */
    EDIT,

    /**
     * Entwürfe veröffentlichen und veröffentlichte Versionen zurückziehen. Getrennt vom Bearbeiten, damit der Host ein
     * Vier-Augen-Prinzip abbilden kann.
     */
    PUBLISH
}
