// erwartet: kompiliert
package proof;

import org.jspecify.annotations.NullMarked;

@NullMarked
class Correct {

    String value() {
        return "present";
    }
}
