// expected: [NullAway] returning @Nullable expression
package proof;

import org.jspecify.annotations.NullMarked;

@NullMarked
class NullReturn {

    String value() {
        return null;
    }
}
