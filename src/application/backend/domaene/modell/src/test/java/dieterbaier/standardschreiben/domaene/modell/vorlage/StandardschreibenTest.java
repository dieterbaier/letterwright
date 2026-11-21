package dieterbaier.standardschreiben.domaene.modell.vorlage;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Die Versionsregeln selbst prüft {@link VorlageTest}; hier geht es um das, was das Standardschreiben ausmacht. */
class StandardschreibenTest {

    private final VorlagenId id = VorlagenId.neu();
    private final VorlagenId briefpapier = VorlagenId.neu();

    @Test
    void kenntGenauEinBriefpapierUndDurchlaeuftDieVersionen() {
        Standardschreiben neu = Standardschreiben.anlegen(id, "Mahnung", briefpapier, "<p>Mahnung</p>");

        Standardschreiben mitV2 = neu.aendereEntwurf("<p>1. Mahnung</p>").veroeffentlicheEntwurf()
                .neuerEntwurf("<p>2</p>").aendereEntwurf("<p>2. Mahnung</p>");
        Standardschreiben ohneV1 = mitV2.veroeffentlicheEntwurf().zurueckziehen(Versionsnummer.ERSTE);

        assertThat(mitV2.briefpapier()).isEqualTo(briefpapier);
        assertThat(mitV2.name()).isEqualTo("Mahnung");
        assertThat(mitV2.id()).isEqualTo(id);
        assertThat(mitV2.versionen()).hasSize(2);
        assertThat(mitV2.entwurf()).get().extracting(Vorlagenversion::inhalt).isEqualTo("<p>2. Mahnung</p>");
        assertThat(mitV2.version(Versionsnummer.ERSTE)).get().extracting(Vorlagenversion::inhalt)
                .isEqualTo("<p>1. Mahnung</p>");
        assertThat(ohneV1.neuesteVeroeffentlichteVersion()).get().extracting(Vorlagenversion::nummer)
                .isEqualTo(new Versionsnummer(2));
    }

    @Test
    void wiederherstellen() {
        Standardschreiben geladen = Standardschreiben.wiederherstellen(id, "Mahnung", briefpapier, List.of(
                Vorlagenversion.wiederherstellen(id, Versionsnummer.ERSTE, Vorlagenzustand.VEROEFFENTLICHT, "x")));

        assertThat(geladen.neuesteVeroeffentlichteVersion()).isPresent();
        lehntAb(() -> Standardschreiben.wiederherstellen(id, "Mahnung", briefpapier, List.of()),
                "mindestens eine Version");
    }

    @Test
    void pflichtattribute() {
        lehntAb(() -> Standardschreiben.anlegen(null, "M", briefpapier, ""), "ID des Standardschreibens");
        lehntAb(() -> Standardschreiben.anlegen(id, "", briefpapier, ""), "Name des Standardschreibens");
        lehntAb(() -> Standardschreiben.anlegen(id, "M", null, ""), "Briefpapier des Standardschreibens");
        lehntAb(() -> Standardschreiben.wiederherstellen(null, "M", briefpapier, List.of()),
                "ID des Standardschreibens");
    }

    @Test
    void gleichheitUeberDieIdentitaet() {
        Standardschreiben neu = Standardschreiben.anlegen(id, "Mahnung", briefpapier, "");

        assertThat(neu.aendereEntwurf("x")).isEqualTo(neu).hasSameHashCodeAs(neu);
        assertThat(Standardschreiben.anlegen(VorlagenId.neu(), "Mahnung", briefpapier, "")).isNotEqualTo(neu);
        assertThat(neu).isNotEqualTo(id);
        assertThat(neu.toString()).contains("Mahnung");
    }
}
