package dieterbaier.standardschreiben.spi;

/** Eine Handlung an Briefpapier und Standardschreiben. Sie hat keinen fachlichen Bezug. */
public enum VorlagenAktion implements Aktion {

    /** Briefpapier und Standardschreiben anlegen, Entwürfe ändern und neue Entwürfe anlegen. */
    BEARBEITEN,

    /**
     * Entwürfe veröffentlichen und veröffentlichte Versionen zurückziehen. Getrennt vom Bearbeiten, damit der Host ein
     * Vier-Augen-Prinzip abbilden kann.
     */
    VEROEFFENTLICHEN
}
