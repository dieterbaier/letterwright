package dieterbaier.standardschreiben.archfixture.validierung;

import dieterbaier.standardschreiben.archfixture.domaene.DomaeneMitAdapter;

/** Fixture: absichtlicher Verstoß, das allgemeine Prüfmodul kennt die Domäne. */
public class ValidierungMitDomaene {

    public DomaeneMitAdapter domaene() {
        return new DomaeneMitAdapter();
    }
}
