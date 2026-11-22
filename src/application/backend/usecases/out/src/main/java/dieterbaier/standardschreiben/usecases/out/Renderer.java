package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Vorlagenversion;

/** Führt Vorlagen und Datenkontext zusammen. Implementiert von {@code adapter-rendering} (ADR-004, ADR-005). */
public interface Renderer {

    /** Das HTML der Vorschau; paginiert wird im Browser (ADR-005). */
    String zusammenfuehren(Vorlagenversion briefpapier, Vorlagenversion standardschreiben, Datenkontext datenkontext);

    /** Das PDF desselben Inhalts, ohne Konformitätsprüfung; die macht der {@link KonformitaetsPruefer}. */
    byte[] erzeugePdf(Vorlagenversion briefpapier, Vorlagenversion standardschreiben, Datenkontext datenkontext);

    /**
     * Das Probe-PDF eines Briefpapiers vor dem Veröffentlichen: mit einem eingebauten Beispielinhalt an der Stelle des
     * Standardschreibens und den Beispieldaten des Briefpapiers (ADR-022). Ohne Konformitätsprüfung.
     */
    byte[] erzeugeProbePdf(Vorlagenversion briefpapier, Datenkontext beispieldaten);
}
