package dieterbaier.standardschreiben.spi;

/**
 * Eine Handlung, für die der Host die Berechtigung erteilt. Eine {@link VorlagenAktion} bezieht sich auf kein
 * Domänenobjekt des Hosts, eine {@link SchreibenAktion} immer auf genau einen {@link FachlicherBezug}.
 */
public sealed interface Aktion permits VorlagenAktion, SchreibenAktion {

    /** Der Name der Konstante, etwa {@code ERZEUGEN}. */
    String name();
}
