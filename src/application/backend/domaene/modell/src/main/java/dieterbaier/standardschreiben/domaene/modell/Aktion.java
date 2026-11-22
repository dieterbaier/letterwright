package dieterbaier.standardschreiben.domaene.modell;

/**
 * Eine Handlung, für die die Host-Anwendung die Berechtigung erteilt (ADR-002). Rollen und Sichtbarkeit bestimmt der
 * Host (Q-PO-003); die Bibliothek fragt je Handlung. Ob eine Handlung einen fachlichen Bezug hat, sagt ihr Typ.
 */
public sealed interface Aktion permits VorlagenAktion, SchreibenAktion {
}
