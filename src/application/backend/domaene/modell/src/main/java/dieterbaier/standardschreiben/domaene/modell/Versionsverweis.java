package dieterbaier.standardschreiben.domaene.modell;

/** Verweist auf genau eine Version eines Briefpapiers oder Standardschreibens. */
public record Versionsverweis(VorlagenId vorlage, Versionsnummer nummer) {
}
