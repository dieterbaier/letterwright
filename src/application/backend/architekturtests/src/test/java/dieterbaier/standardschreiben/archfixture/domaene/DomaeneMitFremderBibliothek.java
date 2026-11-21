package dieterbaier.standardschreiben.archfixture.domaene;

import org.assertj.core.util.Lists;

/** Fixture: absichtlicher Verstoß, eine Domänenklasse mit einer nicht zugelassenen Bibliothek. */
public class DomaeneMitFremderBibliothek {

    public Object liste() {
        return Lists.newArrayList("a");
    }
}
