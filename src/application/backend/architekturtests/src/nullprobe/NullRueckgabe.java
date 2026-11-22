// erwartet: [NullAway] returning @Nullable expression
package probe;

import org.jspecify.annotations.NullMarked;

@NullMarked
class NullRueckgabe {

    String wert() {
        return null;
    }
}
