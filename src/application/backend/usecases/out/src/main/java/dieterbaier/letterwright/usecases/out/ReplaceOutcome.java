package dieterbaier.letterwright.usecases.out;

/** Was das {@link TemplateRepository} beim bedingten Ersetzen einer Vorlage meldet (ADR-022). */
public sealed interface ReplaceOutcome {

    record Replaced() implements ReplaceOutcome {}

    /** Ersetzt wurde nichts; die gespeicherte Vorlage hat sich seit dem Laden geändert oder ist nicht mehr vorhanden. */
    record Stale() implements ReplaceOutcome {}
}
