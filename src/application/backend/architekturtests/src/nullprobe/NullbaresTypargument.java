// erwartet: [NullAway] incompatible types: List<@Nullable String>
package probe;

import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class NullbaresTypargument {

    static int laenge(List<String> texte) {
        return texte.getFirst().length();
    }

    static int rufe(List<@Nullable String> texte) {
        return laenge(texte);
    }
}
