package dieterbaier.letterwright.spi;

import java.util.UUID;

/**
 * The version of the letter template a letter was created from. Versions are immutable; the ID stays the same across
 * all versions of a letter template and is the same as in {@link LetterTemplateId}.
 *
 * @param id the ID of the letter template
 * @param number the sequential number of the version, starting at 1
 */
public record LetterTemplateVersion(UUID id, int number) {

    public LetterTemplateVersion {
        Require.value(id, "The ID of the letter template");
        if (number < 1) {
            throw new IllegalArgumentException("The version number must be at least 1, but is " + number);
        }
    }
}
