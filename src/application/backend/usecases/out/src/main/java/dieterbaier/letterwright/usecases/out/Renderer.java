package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.template.TemplateVersion;

/** Führt Vorlagen und Datenkontext zusammen. Implementiert von {@code adapter-rendering} (ADR-004, ADR-005). */
public interface Renderer {

    /** Das HTML der Vorschau; paginiert wird im Browser (ADR-005). */
    String merge(TemplateVersion letterhead, TemplateVersion letterTemplate, DataContext dataContext);

    /** Das PDF desselben Inhalts, ohne Konformitätsprüfung; die macht der {@link ConformanceChecker}. */
    byte[] createPdf(TemplateVersion letterhead, TemplateVersion letterTemplate, DataContext dataContext);

    /**
     * Das Probe-PDF eines Briefpapiers vor dem Veröffentlichen: mit einem eingebauten Beispielinhalt an der Stelle des
     * Standardschreibens und den Beispieldaten des Briefpapiers (ADR-022). Ohne Konformitätsprüfung.
     */
    byte[] createProofPdf(TemplateVersion letterhead, DataContext sampleData);
}
