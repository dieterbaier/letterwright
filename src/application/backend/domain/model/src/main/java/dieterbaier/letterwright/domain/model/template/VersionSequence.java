package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.validation.Validate;
import dieterbaier.letterwright.validation.ValidationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Die Versionen einer Vorlage und die Regeln über sie hinweg. Briefpapier und Standardschreiben teilen sich diese
 * Regeln; nach außen sichtbar sind sie über die beiden Aggregate.
 *
 * <p>Invarianten:
 * <ul>
 *   <li>Mindestens eine Version; alle gehören zur selben Vorlage.</li>
 *   <li>Die Nummern laufen lückenlos von 1 an, jede gibt es genau einmal.</li>
 *   <li>Höchstens ein Entwurf, und wenn, dann ist er die neueste Version.</li>
 *   <li>Höchstens eine veröffentlichte Version, und wenn, dann ist sie die neueste freigegebene (ADR-022).</li>
 *   <li>Eine abgelöste Version hat eine neuere freigegebene Version.</li>
 * </ul>
 *
 * <p>Freigegeben heißt: kein Entwurf mehr, also veröffentlicht, abgelöst oder zurückgezogen.
 */
final class VersionSequence {

    private final TemplateId template;
    private final List<TemplateVersion> versions;

    private VersionSequence(TemplateId template, List<TemplateVersion> versions) {
        this.template = template;
        this.versions = List.copyOf(versions);
    }

    static VersionSequence first(TemplateId template, String content) {
        return new VersionSequence(template, List.of(
                TemplateVersion.draft(template, VersionNumber.FIRST, content, DataContext.from(Map.of()))));
    }

    static VersionSequence restore(TemplateId template, List<TemplateVersion> versions) {
        Validate.requiredWithoutGaps(versions, "Die Versionen");
        if (versions.isEmpty()) {
            throw new ValidationException("Eine Vorlage hat mindestens eine Version");
        }
        List<TemplateVersion> sorted = new ArrayList<>(versions);
        sorted.sort(Comparator.comparing(TemplateVersion::number));
        boolean withDraft = sorted.getLast().state() == TemplateVersionState.DRAFT;
        int newestReleased = sorted.size() - (withDraft ? 2 : 1);
        VersionNumber expected = VersionNumber.FIRST;
        for (int position = 0; position < sorted.size(); position++) {
            TemplateVersion version = sorted.get(position);
            if (!version.template().equals(template)) {
                throw new ValidationException("Version " + version.number().value() + " gehört zur Vorlage "
                        + version.template().value() + ", nicht zu " + template.value());
            }
            if (!version.number().equals(expected)) {
                throw new ValidationException("Die Versionsnummern laufen nicht lückenlos von 1 an: erwartet "
                        + expected.value() + ", gefunden " + version.number().value());
            }
            boolean isNewest = position == sorted.size() - 1;
            if (version.state() == TemplateVersionState.DRAFT && !isNewest) {
                throw new ValidationException("Version " + version.number().value()
                        + " ist ein Entwurf, aber nicht die neueste Version");
            }
            if (version.state() == TemplateVersionState.PUBLISHED && position != newestReleased) {
                throw new ValidationException("Version " + version.number().value() + " ist veröffentlicht, aber nicht "
                        + "die neueste freigegebene Version; je Vorlage ist höchstens eine veröffentlicht");
            }
            if (version.state() == TemplateVersionState.SUPERSEDED && position == newestReleased) {
                throw new ValidationException("Version " + version.number().value()
                        + " ist abgelöst, aber keine neuere Version ist freigegeben");
            }
            expected = expected.next();
        }
        return new VersionSequence(template, sorted);
    }

    List<TemplateVersion> all() {
        return versions;
    }

    Optional<TemplateVersion> version(VersionNumber number) {
        return versions.stream().filter(version -> version.number().equals(number)).findFirst();
    }

    Optional<TemplateVersion> draft() {
        TemplateVersion newest = versions.getLast();
        return newest.state() == TemplateVersionState.DRAFT ? Optional.of(newest) : Optional.empty();
    }

    Optional<TemplateVersion> published() {
        return versions.stream()
                .filter(version -> version.state() == TemplateVersionState.PUBLISHED)
                .findFirst();
    }

    VersionSequence changeDraft(String content) {
        TemplateVersion draft = requireDraft("Ändern");
        return replace(draft.withContent(content, draft.sampleData()));
    }

    VersionSequence changeDraft(String content, DataContext sampleData) {
        return replace(requireDraft("Ändern").withContent(content, sampleData));
    }

    VersionSequence publishDraft() {
        TemplateVersion draft = requireDraft("Veröffentlichen");
        if (!draft.isPublishable()) {
            throw new IllegalStateTransition(template,
                    "Ein Entwurf ohne Inhalt kann nicht veröffentlicht werden");
        }
        VersionSequence superseded = published()
                .map(previous -> replace(previous.inState(TemplateVersionState.SUPERSEDED)))
                .orElse(this);
        return superseded.replace(draft.inState(TemplateVersionState.PUBLISHED));
    }

    VersionSequence newDraft(String content) {
        draft().ifPresent(present -> {
            throw new IllegalStateTransition(template, "Version " + present.number().value()
                    + " ist schon ein Entwurf; es gibt höchstens einen");
        });
        TemplateVersion newest = versions.getLast();
        List<TemplateVersion> generate = new ArrayList<>(versions);
        generate.add(TemplateVersion.draft(template, newest.number().next(), content, newest.sampleData()));
        return new VersionSequence(template, generate);
    }

    VersionSequence withdraw() {
        TemplateVersion published = published().orElseThrow(() -> new IllegalStateTransition(
                template, "Zurückziehen braucht eine veröffentlichte Version; es gibt keine"));
        return replace(published.inState(TemplateVersionState.WITHDRAWN));
    }

    private TemplateVersion requireDraft(String case_) {
        return draft().orElseThrow(() -> new IllegalStateTransition(template,
                case_ + " braucht einen Entwurf; die neueste Version ist " + versions.getLast().state()));
    }

    private VersionSequence replace(TemplateVersion changed) {
        List<TemplateVersion> generate = new ArrayList<>(versions);
        generate.replaceAll(version -> version.equals(changed) ? changed : version);
        return new VersionSequence(template, generate);
    }
}
