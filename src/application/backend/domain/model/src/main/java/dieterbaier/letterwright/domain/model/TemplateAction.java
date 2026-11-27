package dieterbaier.letterwright.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;

/** An action on letterheads and letter templates, without a business reference. */
@ValueObject
public enum TemplateAction {

    /** Covers creating, changing drafts and creating new drafts. */
    EDIT,

    /**
     * Also covers withdrawing published versions. Separate from editing so that the host can enforce a four-eyes
     * principle: what is published goes into every new letter.
     */
    PUBLISH
}
