package dieterbaier.standardschreiben.usecases.out;

/** Was das {@link VorlagenRepository} beim bedingten Ersetzen einer Vorlage meldet (ADR-022). */
public sealed interface Ersetzungsausgang {

    record Ersetzt() implements Ersetzungsausgang {}

    /** Ersetzt wurde nichts; die gespeicherte Vorlage hat sich seit dem Laden geändert oder ist nicht mehr vorhanden. */
    record Veraltet() implements Ersetzungsausgang {}
}
