package dieterbaier.standardschreiben.domaene.modell.vorlage;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Die Regeln über alle Versionen einer Vorlage, geprüft am Briefpapier. Das Standardschreiben teilt sie. */
class VorlageTest {

    private final VorlagenId id = VorlagenId.neu();
    private final Briefpapier neu = Briefpapier.anlegenMitStandardtyp(id, "Geschäftsbrief", "<p>Hallo</p>");

    private static Vorlagenversion version(VorlagenId vorlage, int nummer, Vorlagenzustand zustand) {
        return Vorlagenversion.wiederherstellen(vorlage, new Versionsnummer(nummer), zustand, "<p>" + nummer + "</p>");
    }

    @Test
    void einNeuesBriefpapierHatDieErsteVersionAlsEntwurf() {
        assertThat(neu.typ()).isEqualTo(Briefpapiertyp.GESCHAEFTLICH_FORM_B);
        assertThat(neu.versionen()).singleElement().satisfies(version -> {
            assertThat(version.nummer()).isEqualTo(Versionsnummer.ERSTE);
            assertThat(version.zustand()).isEqualTo(Vorlagenzustand.ENTWURF);
            assertThat(version.inhalt()).isEqualTo("<p>Hallo</p>");
            assertThat(version.verweis()).isEqualTo(new Versionsverweis(id, Versionsnummer.ERSTE));
        });
        assertThat(neu.entwurf()).isPresent();
        assertThat(neu.neuesteVeroeffentlichteVersion()).isEmpty();
    }

    @Test
    void einEntwurfIstAenderbarUndDarfLeerSein() {
        Briefpapier geaendert = neu.aendereEntwurf("");

        assertThat(geaendert.entwurf()).get().extracting(Vorlagenversion::inhalt).isEqualTo("");
        assertThat(neu.entwurf()).get().extracting(Vorlagenversion::inhalt)
                .as("die Ausgangsinstanz bleibt unverändert").isEqualTo("<p>Hallo</p>");
    }

    @Test
    void einFehlenderInhaltWirdAbgelehnt() {
        lehntAb(() -> Briefpapier.anlegenMitStandardtyp(id, "Geschäftsbrief", null), "Inhalt der Version");
        lehntAb(() -> neu.aendereEntwurf(null), "Inhalt der Version");
        lehntAb(() -> neu.veroeffentlicheEntwurf().neuerEntwurf(null), "Inhalt der Version");
    }

    @Test
    void einLeererEntwurfLaesstSichNichtVeroeffentlichen() {
        Briefpapier leer = neu.aendereEntwurf("  ");

        assertThatThrownBy(leer::veroeffentlicheEntwurf)
                .isInstanceOf(UnzulaessigerZustandswechsel.class)
                .hasMessageContaining("ohne Inhalt");
    }

    @Test
    void lebenszyklusEntwurfVeroeffentlichtZurueckgezogen() {
        Briefpapier veroeffentlicht = neu.veroeffentlicheEntwurf();
        Briefpapier zurueckgezogen = veroeffentlicht.zurueckziehen(Versionsnummer.ERSTE);

        assertThat(veroeffentlicht.entwurf()).isEmpty();
        assertThat(veroeffentlicht.neuesteVeroeffentlichteVersion()).get()
                .extracting(Vorlagenversion::nummer).isEqualTo(Versionsnummer.ERSTE);
        assertThat(zurueckgezogen.version(Versionsnummer.ERSTE)).get()
                .extracting(Vorlagenversion::zustand).isEqualTo(Vorlagenzustand.ZURUECKGEZOGEN);
        assertThat(zurueckgezogen.neuesteVeroeffentlichteVersion()).isEmpty();
    }

    @Test
    void eineVeroeffentlichteVersionIstUnveraenderlich() {
        Briefpapier veroeffentlicht = neu.veroeffentlicheEntwurf();

        assertThatThrownBy(() -> veroeffentlicht.aendereEntwurf("<p>anders</p>"))
                .isInstanceOf(UnzulaessigerZustandswechsel.class)
                .hasMessageContaining("Ändern braucht einen Entwurf")
                .hasMessageContaining("VEROEFFENTLICHT");
        assertThatThrownBy(veroeffentlicht::veroeffentlicheEntwurf).isInstanceOf(UnzulaessigerZustandswechsel.class);
    }

    @Test
    void eineAenderungNachDemVeroeffentlichenIstDerNaechsteEntwurf() {
        Briefpapier mitV2 = neu.veroeffentlicheEntwurf().neuerEntwurf("<p>neu</p>");

        assertThat(mitV2.versionen()).extracting(Vorlagenversion::nummer)
                .containsExactly(Versionsnummer.ERSTE, new Versionsnummer(2));
        assertThat(mitV2.entwurf()).get().extracting(Vorlagenversion::nummer).isEqualTo(new Versionsnummer(2));
        assertThat(mitV2.neuesteVeroeffentlichteVersion()).get()
                .extracting(Vorlagenversion::nummer).isEqualTo(Versionsnummer.ERSTE);
    }

    @Test
    void esGibtHoechstensEinenEntwurf() {
        assertThatThrownBy(() -> neu.neuerEntwurf("<p>zweiter</p>"))
                .isInstanceOf(UnzulaessigerZustandswechsel.class)
                .hasMessageContaining("höchstens einen");
    }

    @Test
    void neueNummernFolgenAufDieHoechsteAuchNachDemZurueckziehen() {
        Briefpapier mitV2 = neu.veroeffentlicheEntwurf().zurueckziehen(Versionsnummer.ERSTE).neuerEntwurf("<p>2</p>");

        assertThat(mitV2.entwurf()).get().extracting(Vorlagenversion::nummer).isEqualTo(new Versionsnummer(2));
    }

    @Test
    void zurueckziehenGehtNurBeiVeroeffentlichtenVersionen() {
        assertThatThrownBy(() -> neu.zurueckziehen(Versionsnummer.ERSTE))
                .isInstanceOf(UnzulaessigerZustandswechsel.class)
                .hasMessageContaining("ENTWURF");
        Briefpapier zurueckgezogen = neu.veroeffentlicheEntwurf().zurueckziehen(Versionsnummer.ERSTE);
        assertThatThrownBy(() -> zurueckgezogen.zurueckziehen(Versionsnummer.ERSTE))
                .isInstanceOf(UnzulaessigerZustandswechsel.class);
        lehntAb(() -> neu.zurueckziehen(new Versionsnummer(9)), "Version 9 gibt es nicht");
        lehntAb(() -> neu.zurueckziehen(null), "Versionsnummer");
    }

    @Test
    void wiederherstellenSortiertNachNummer() {
        Briefpapier geladen = Briefpapier.wiederherstellen(id, "Geschäftsbrief", Briefpapiertyp.PRIVAT, List.of(
                version(id, 2, Vorlagenzustand.ENTWURF),
                version(id, 1, Vorlagenzustand.VEROEFFENTLICHT)));

        assertThat(geladen.versionen()).extracting(version -> version.nummer().wert()).containsExactly(1, 2);
        assertThat(geladen.typ()).isEqualTo(Briefpapiertyp.PRIVAT);
    }

    @Test
    void wiederherstellenPrueftDieRegelnUeberAlleVersionen() {
        VorlagenId fremd = VorlagenId.neu();
        Briefpapiertyp typ = Briefpapiertyp.PRIVAT;

        lehntAb(() -> Briefpapier.wiederherstellen(id, "B", typ, List.of()), "mindestens eine Version");
        lehntAb(() -> Briefpapier.wiederherstellen(id, "B", typ, null), "Die Versionen");
        lehntAb(() -> Briefpapier.wiederherstellen(id, "B", typ,
                Arrays.asList(version(id, 1, Vorlagenzustand.ENTWURF), null)), "Die Versionen[1]");
        lehntAb(() -> Briefpapier.wiederherstellen(id, "B", typ,
                List.of(version(fremd, 1, Vorlagenzustand.ENTWURF))), "gehört zur Vorlage");
        lehntAb(() -> Briefpapier.wiederherstellen(id, "B", typ,
                List.of(version(id, 2, Vorlagenzustand.ENTWURF))), "erwartet 1, gefunden 2");
        lehntAb(() -> Briefpapier.wiederherstellen(id, "B", typ, List.of(
                version(id, 1, Vorlagenzustand.VEROEFFENTLICHT), version(id, 1, Vorlagenzustand.VEROEFFENTLICHT))),
                "erwartet 2, gefunden 1");
        lehntAb(() -> Briefpapier.wiederherstellen(id, "B", typ, List.of(
                version(id, 1, Vorlagenzustand.ENTWURF), version(id, 2, Vorlagenzustand.VEROEFFENTLICHT))),
                "nicht die neueste");
    }

    @Test
    void pflichtattributeDesBriefpapiers() {
        lehntAb(() -> Briefpapier.anlegen(null, "B", Briefpapiertyp.PRIVAT, ""), "ID des Briefpapiers");
        lehntAb(() -> Briefpapier.anlegen(id, null, Briefpapiertyp.PRIVAT, ""), "Name des Briefpapiers");
        lehntAb(() -> Briefpapier.anlegen(id, " ", Briefpapiertyp.PRIVAT, ""), "Name des Briefpapiers");
        lehntAb(() -> Briefpapier.anlegen(id, "B", null, ""), "Typ des Briefpapiers");
        lehntAb(() -> Briefpapier.wiederherstellen(null, "B", Briefpapiertyp.PRIVAT, List.of()),
                "ID des Briefpapiers");
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
    void gleichheitDerAggregateUeberDieIdentitaet() {
        Briefpapier andererStand = neu.aendereEntwurf("<p>anders</p>").veroeffentlicheEntwurf();
        Briefpapier gleicherInhaltAndereId = Briefpapier.anlegenMitStandardtyp(VorlagenId.neu(), "Geschäftsbrief",
                "<p>Hallo</p>");

        assertThat(andererStand).isEqualTo(neu).hasSameHashCodeAs(neu);
        assertThat(gleicherInhaltAndereId).isNotEqualTo(neu);
        assertThat(neu).isNotEqualTo("Geschäftsbrief");
        assertThat(neu.toString()).contains(id.wert().toString(), "Geschäftsbrief");
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
