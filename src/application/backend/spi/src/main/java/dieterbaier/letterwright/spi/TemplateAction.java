package dieterbaier.letterwright.spi;

/** An action on letterheads and letter templates. It has no business reference. */
public enum TemplateAction {

    /** Create letterheads and letter templates, change drafts and create new drafts. */
    EDIT,

    /**
     * Publish drafts and withdraw published versions. Separate from editing so that the host can enforce a
     * four-eyes principle.
     */
    PUBLISH
}
