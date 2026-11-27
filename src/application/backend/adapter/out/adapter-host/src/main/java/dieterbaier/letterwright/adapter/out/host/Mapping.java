package dieterbaier.letterwright.adapter.out.host;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.TemplateAction;
import dieterbaier.letterwright.domain.model.letter.DeletionDecision;
import dieterbaier.letterwright.domain.model.letter.CallbackResult;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.spi.DataRequest;
import dieterbaier.letterwright.spi.SavedLetter;
import dieterbaier.letterwright.spi.DeletionRequest;
import dieterbaier.letterwright.spi.LetterTemplateId;
import dieterbaier.letterwright.spi.LetterTemplateVersion;
import dieterbaier.letterwright.validation.ValidationException;

/**
 * Maps between the types of the SPI and those of the domain model, in both directions (ADR-016). The types carry the
 * same domain terms; the SPI types are fully qualified here, the domain types are imported.
 */
final class Mapping {

    private Mapping() {
    }

    static dieterbaier.letterwright.spi.BusinessReference toSpi(BusinessReference reference) {
        return new dieterbaier.letterwright.spi.BusinessReference(reference.type(), reference.id());
    }

    static BusinessReference toDomain(dieterbaier.letterwright.spi.BusinessReference reference) {
        return new BusinessReference(reference.type(), reference.id());
    }

    static dieterbaier.letterwright.spi.TemplateAction toSpi(TemplateAction action) {
        return switch (action) {
            case EDIT -> dieterbaier.letterwright.spi.TemplateAction.EDIT;
            case PUBLISH -> dieterbaier.letterwright.spi.TemplateAction.PUBLISH;
        };
    }

    static dieterbaier.letterwright.spi.LetterAction toSpi(LetterAction action) {
        return switch (action) {
            case VIEW -> dieterbaier.letterwright.spi.LetterAction.VIEW;
            case DOWNLOAD -> dieterbaier.letterwright.spi.LetterAction.DOWNLOAD;
            case DELETE -> dieterbaier.letterwright.spi.LetterAction.DELETE;
        };
    }

    static dieterbaier.letterwright.spi.DeletionDecision toSpi(DeletionDecision decision) {
        return switch (decision) {
            case DeletionDecision.Hard hard -> dieterbaier.letterwright.spi.DeletionDecision.hard();
            case DeletionDecision.Logical logical -> dieterbaier.letterwright.spi.DeletionDecision.logical();
            case DeletionDecision.Blocked blocked ->
                    dieterbaier.letterwright.spi.DeletionDecision.blocked(blocked.reason());
        };
    }

    static DeletionDecision toDomain(dieterbaier.letterwright.spi.DeletionDecision decision) {
        return switch (decision) {
            case dieterbaier.letterwright.spi.DeletionDecision.Hard hard -> DeletionDecision.hard();
            case dieterbaier.letterwright.spi.DeletionDecision.Logical logical -> DeletionDecision.logical();
            case dieterbaier.letterwright.spi.DeletionDecision.Blocked blocked ->
                    DeletionDecision.blocked(blocked.reason());
        };
    }

    static dieterbaier.letterwright.spi.DataContext toSpi(DataContext dataContext) {
        return new dieterbaier.letterwright.spi.DataContext(dataContext.values());
    }

    /**
     * Takes the host's values over into the domain model, which checks them.
     *
     * @throws HostContractViolated if the values violate the rules of the data context
     */
    static DataContext toDomain(dieterbaier.letterwright.spi.DataContext dataContext) {
        try {
            return DataContext.from(dataContext.values());
        } catch (ValidationException error) {
            throw new HostContractViolated("The PlaceholderDataProvider of the host delivered an invalid"
                    + " DataContext: " + error.getMessage(), error);
        }
    }

    static LetterTemplateId id(LetterTemplate letterTemplate) {
        return new LetterTemplateId(letterTemplate.id().value(), letterTemplate.name());
    }

    static DataRequest dataRequest(UserId user, BusinessReference reference, LetterTemplate template) {
        return new DataRequest(user.value(), toSpi(reference), id(template));
    }

    static CallbackResult toDomain(dieterbaier.letterwright.spi.CallbackResult result) {
        return switch (result) {
            case dieterbaier.letterwright.spi.CallbackResult.Accepted accepted ->
                    CallbackResult.accepted();
            case dieterbaier.letterwright.spi.CallbackResult.Rejected rejected ->
                    CallbackResult.rejected(rejected.reason());
        };
    }

    /**
     * The letter itself does not carry a user ID. The callback runs only on creation, and a letter is created only from
     * the user's own snapshot; the calling user has therefore created the letter.
     */
    static SavedLetter savedLetter(UserId user, Letter letter) {
        return new SavedLetter(letter.id().value(), toSpi(letter.reference()),
                new LetterTemplateVersion(letter.letterTemplate().template().value(),
                        letter.letterTemplate().number().value()),
                user.value(), letter.created());
    }

    static DeletionRequest deletionRequest(UserId user, Letter letter) {
        return new DeletionRequest(user.value(), letter.id().value(), toSpi(letter.reference()),
                letter.created());
    }
}
