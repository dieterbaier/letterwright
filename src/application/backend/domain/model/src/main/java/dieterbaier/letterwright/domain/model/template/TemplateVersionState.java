package dieterbaier.letterwright.domain.model.template;

import org.jmolecules.ddd.annotation.ValueObject;

/** State of a template version (ADR-007). */
@ValueObject
public enum TemplateVersionState {

    /** Being edited, changeable, not visible to clerks. Each template has at most one. */
    DRAFT,

    /** Released and immutable. New letters are created with it; each template has at most one (ADR-022). */
    PUBLISHED,

    /**
     * Replaced by a newer published version. No longer usable for new letters; existing letters still refer to it.
     */
    SUPERSEDED,

    /**
     * Withdrawn without a successor. No longer usable for new letters; existing letters still refer to it.
     */
    WITHDRAWN
}
