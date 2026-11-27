package dieterbaier.letterwright.archfixture.shared;

/** Fixture: class in a package that no rule names explicitly. */
public final class SharedHelper {

    private SharedHelper() {
    }

    public static String normalize(String value) {
        return value.strip();
    }
}
