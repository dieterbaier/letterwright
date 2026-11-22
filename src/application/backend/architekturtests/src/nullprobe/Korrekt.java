// erwartet: kompiliert
package probe;

import org.jspecify.annotations.NullMarked;

@NullMarked
class Korrekt {

    String wert() {
        return "vorhanden";
    }
}
