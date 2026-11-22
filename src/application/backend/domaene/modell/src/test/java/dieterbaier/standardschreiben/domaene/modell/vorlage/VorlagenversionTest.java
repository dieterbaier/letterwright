package dieterbaier.standardschreiben.domaene.modell.vorlage;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VorlagenversionTest {

    private final VorlagenId id = VorlagenId.neu();

    private static Vorlagenversion version(VorlagenId vorlage, int nummer, Vorlagenzustand zustand) {
        return Vorlagenversion.wiederherstellen(vorlage, new Versionsnummer(nummer), zustand, "<p>" + nummer + "</p>");
    }

    @Test
    void pflichtattributeDerVersion() {
        Versionsnummer eins = Versionsnummer.ERSTE;
        Vorlagenzustand entwurf = Vorlagenzustand.ENTWURF;

        lehntAb(() -> Vorlagenversion.wiederherstellen(null, eins, entwurf, ""), "Vorlage der Version");
        lehntAb(() -> Vorlagenversion.wiederherstellen(id, null, entwurf, ""), "Versionsnummer");
        lehntAb(() -> Vorlagenversion.wiederherstellen(id, eins, null, ""), "Zustand der Version");
        lehntAb(() -> Vorlagenversion.wiederherstellen(id, eins, entwurf, null), "Inhalt der Version");
        lehntAb(() -> Vorlagenversion.wiederherstellen(id, eins, Vorlagenzustand.VEROEFFENTLICHT, ""),
                "Inhalt einer veröffentlichten");
    }

    @Test
    void gleichheitDerVersionenUeberVorlageUndNummer() {
        Vorlagenversion entwurf = version(id, 1, Vorlagenzustand.ENTWURF);
        Vorlagenversion veroeffentlicht = version(id, 1, Vorlagenzustand.VEROEFFENTLICHT);

        assertThat(veroeffentlicht).isEqualTo(entwurf).hasSameHashCodeAs(entwurf);
        assertThat(version(id, 2, Vorlagenzustand.ENTWURF)).isNotEqualTo(entwurf);
        assertThat(version(VorlagenId.neu(), 1, Vorlagenzustand.ENTWURF)).isNotEqualTo(entwurf);
        assertThat(entwurf).isNotEqualTo(entwurf.verweis());
        assertThat(entwurf.toString()).contains("nummer=1", "ENTWURF").doesNotContain("<p>");
    }
}
