package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.template.TemplateVersion;

/** Merges templates and data context. Implemented by {@code adapter-rendering} (ADR-004, ADR-005). */
public interface Renderer {

    /** The HTML of the preview; pagination happens in the browser (ADR-005). */
    String merge(TemplateVersion letterhead, TemplateVersion letterTemplate, DataContext dataContext);

    /** The PDF of the same content, without a conformance check; the {@link ConformanceChecker} does that. */
    byte[] createPdf(TemplateVersion letterhead, TemplateVersion letterTemplate, DataContext dataContext);

    /**
     * The proof PDF of a letterhead before publishing: with built-in sample content in place of the letter template and
     * the letterhead's sample data (ADR-022). Without a conformance check.
     */
    byte[] createProofPdf(TemplateVersion letterhead, DataContext sampleData);
}
