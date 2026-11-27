package dieterbaier.letterwright.archfixture.shared;

/** Fixture: Klasse in einem Paket, das keine Regel namentlich nennt. */
public final class SharedHelper {

    private SharedHelper() {
    }

    public static String normalize(String value) {
        return value.strip();
    }
}
