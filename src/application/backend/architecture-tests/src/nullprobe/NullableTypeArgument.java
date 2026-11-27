// expected: [NullAway] incompatible types: List<@Nullable String>
package proof;

import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class NullableTypeArgument {

    static int length(List<String> texts) {
        return texts.getFirst().length();
    }

    static int call(List<@Nullable String> texts) {
        return length(texts);
    }
}
