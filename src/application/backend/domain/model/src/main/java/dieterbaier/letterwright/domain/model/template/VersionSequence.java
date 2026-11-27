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
 * The versions of a template and the rules across them. Letterhead and letter template share these rules; they are
 * visible to the outside through the two aggregates.
 *
 * <p>Invariants:
 * <ul>
 *   <li>At least one version; all belong to the same template.</li>
 *   <li>The numbers run from 1 without gaps; each exists exactly once.</li>
 *   <li>At most one draft, and if there is one, it is the newest version.</li>
 *   <li>At most one published version, and if there is one, it is the newest released one (ADR-022).</li>
 *   <li>A superseded version has a newer released version.</li>
 * </ul>
 *
 * <p>Released means: no longer a draft, i.e. published, superseded or withdrawn.
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
        Validate.requiredWithoutGaps(versions, "The versions");
        if (versions.isEmpty()) {
            throw new ValidationException("A template has at least one version");
        }
        List<TemplateVersion> sorted = new ArrayList<>(versions);
        sorted.sort(Comparator.comparing(TemplateVersion::number));
        boolean withDraft = sorted.getLast().state() == TemplateVersionState.DRAFT;
        int newestReleased = sorted.size() - (withDraft ? 2 : 1);
        VersionNumber expected = VersionNumber.FIRST;
        for (int position = 0; position < sorted.size(); position++) {
            TemplateVersion version = sorted.get(position);
            if (!version.template().equals(template)) {
                throw new ValidationException("Version " + version.number().value() + " belongs to template "
                        + version.template().value() + ", not to " + template.value());
            }
            if (!version.number().equals(expected)) {
                throw new ValidationException("The version numbers do not run from 1 without gaps: expected "
                        + expected.value() + ", found " + version.number().value());
            }
            boolean isNewest = position == sorted.size() - 1;
            if (version.state() == TemplateVersionState.DRAFT && !isNewest) {
                throw new ValidationException("Version " + version.number().value()
                        + " is a draft but not the newest version");
            }
            if (version.state() == TemplateVersionState.PUBLISHED && position != newestReleased) {
                throw new ValidationException("Version " + version.number().value() + " is published but not "
                        + "the newest released version; at most one version per template is published");
            }
            if (version.state() == TemplateVersionState.SUPERSEDED && position == newestReleased) {
                throw new ValidationException("Version " + version.number().value()
                        + " is superseded, but no newer version is released");
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
        TemplateVersion draft = requireDraft("Changing");
        return replace(draft.withContent(content, draft.sampleData()));
    }

    VersionSequence changeDraft(String content, DataContext sampleData) {
        return replace(requireDraft("Changing").withContent(content, sampleData));
    }

    VersionSequence publishDraft() {
        TemplateVersion draft = requireDraft("Publishing");
        if (!draft.isPublishable()) {
            throw new IllegalStateTransition(template,
                    "A draft without content cannot be published");
        }
        VersionSequence superseded = published()
                .map(previous -> replace(previous.inState(TemplateVersionState.SUPERSEDED)))
                .orElse(this);
        return superseded.replace(draft.inState(TemplateVersionState.PUBLISHED));
    }

    VersionSequence newDraft(String content) {
        draft().ifPresent(present -> {
            throw new IllegalStateTransition(template, "Version " + present.number().value()
                    + " is already a draft; there is at most one");
        });
        TemplateVersion newest = versions.getLast();
        List<TemplateVersion> extended = new ArrayList<>(versions);
        extended.add(TemplateVersion.draft(template, newest.number().next(), content, newest.sampleData()));
        return new VersionSequence(template, extended);
    }

    VersionSequence withdraw() {
        TemplateVersion published = published().orElseThrow(() -> new IllegalStateTransition(
                template, "Withdrawing requires a published version; there is none"));
        return replace(published.inState(TemplateVersionState.WITHDRAWN));
    }

    private TemplateVersion requireDraft(String operation) {
        return draft().orElseThrow(() -> new IllegalStateTransition(template,
                operation + " requires a draft; the newest version is " + versions.getLast().state()));
    }

    private VersionSequence replace(TemplateVersion changed) {
        List<TemplateVersion> replaced = new ArrayList<>(versions);
        replaced.replaceAll(version -> version.equals(changed) ? changed : version);
        return new VersionSequence(template, replaced);
    }
}
