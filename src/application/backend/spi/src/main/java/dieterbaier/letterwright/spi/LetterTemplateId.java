package dieterbaier.letterwright.spi;

import java.util.UUID;

/** Identifies the letter template for which the host is to deliver data: its ID and its name. */
public record LetterTemplateId(UUID id, String name) {

    public LetterTemplateId {
        Require.value(id, "The ID of the letter template");
        Require.text(name, "The name of the letter template");
    }
}
