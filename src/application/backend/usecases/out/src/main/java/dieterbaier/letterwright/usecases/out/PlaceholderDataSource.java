package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;

/** Liefert den Datenkontext des Hosts in Domänenbegriffen. Implementiert von {@code adapter-host} (ADR-016). */
public interface PlaceholderDataSource {

    /**
     * Liest die bereits formatierten Werte für ein Standardschreiben und einen fachlichen Bezug.
     *
     * @throws IllegalStateException wenn der Host keinen gültigen Datenkontext liefert
     */
    DataContext readDataContext(UserId user, BusinessReference reference, LetterTemplate letterTemplate);
}
