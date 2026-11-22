package dieterbaier.standardschreiben.usecases.out;

/**
 * Prüft ein PDF auf PDF/A-2b und PDF/UA-1 (ADR-008). Implementiert von {@code adapter-rendering}. Kann der Prüfer nicht
 * prüfen, wirft er eine Ausnahme; das zählt nie als konform.
 */
public interface KonformitaetsPruefer {

    Konformitaet pruefe(byte[] pdf);
}
