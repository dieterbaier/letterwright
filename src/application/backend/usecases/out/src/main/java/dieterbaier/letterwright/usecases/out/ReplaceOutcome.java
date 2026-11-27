package dieterbaier.letterwright.usecases.out;

/** What the {@link TemplateRepository} reports on the conditional replacement of a template (ADR-022). */
public sealed interface ReplaceOutcome {

    record Replaced() implements ReplaceOutcome {}

    /** Nothing was replaced; the stored template has changed since it was loaded or no longer exists. */
    record Stale() implements ReplaceOutcome {}
}
