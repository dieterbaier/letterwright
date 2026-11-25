package dieterbaier.standardschreiben.adapter.in.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.api.FachlicherBezug;
import dieterbaier.standardschreiben.api.GespeichertesSchreiben;
import dieterbaier.standardschreiben.api.Speicherergebnis;
import dieterbaier.standardschreiben.api.Standardschreibenversion;
import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreibenzustand;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Brücke zu {@code features/schreiben/schreiben-ueber-die-api-speichern.feature}: ein Test je Szenario, benannt nach
 * dem Szenariotitel. Der Anwendungsfall ist eine Attrappe, die Transaktionen verwaltet die {@link Transaktionsattrappe}.
 * Erwartete Werte baut der Test selbst auf, nicht mit der {@link Abbildung}.
 */
class ApiSchreibenSpeichernTest {

    private static final UUID SCHREIBEN = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MAHNUNG = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID BRIEFPAPIER = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final Instant ERSTELLT = Instant.parse("2026-11-24T10:00:00Z");

    // Grundlage: Erika hat eine Vorschau der Mahnung in Version 2 zum Kunden 4711 erzeugt.
    private final UUID vorschauVonErika = UUID.randomUUID();
    private final Schreiben ausDerVorschau = Schreiben.wiederherstellen(new SchreibenId(SCHREIBEN),
            new SnapshotId(vorschauVonErika),
            new dieterbaier.standardschreiben.domaene.modell.FachlicherBezug("Kunde", "4711"),
            new Versionsverweis(new VorlagenId(BRIEFPAPIER), Versionsnummer.ERSTE),
            new Versionsverweis(new VorlagenId(MAHNUNG), new Versionsnummer(2)), new Dokumentverweis("ablage/1"),
            Pruefsumme.ueber(new byte[] {1}), ERSTELLT, new Schreibenzustand.Sichtbar());

    private final Transaktionsattrappe transaktionen = new Transaktionsattrappe();
    private final Anwendungsfall anwendungsfall = new Anwendungsfall(transaktionen);
    private final ApiSchreibenSpeichern api = new ApiSchreibenSpeichern(anwendungsfall, transaktionen);
    private final TransactionTemplate host = new TransactionTemplate(transaktionen);

    {
        anwendungsfall.ergebnis = () -> new dieterbaier.standardschreiben.usecases.in.Speicherergebnis.Gespeichert(
                ausDerVorschau);
    }

    private static final GespeichertesSchreiben ERWARTET = new GespeichertesSchreiben(SCHREIBEN,
            new FachlicherBezug("Kunde", "4711"), new Standardschreibenversion(MAHNUNG, 2), "erika", ERSTELLT);

    @Test
    void derHostErhaeltDieIdentifikatorenDesGespeichertenSchreibens() {
        var ergebnis = api.speichere("erika", vorschauVonErika);

        assertThat(ergebnis).isEqualTo(new Speicherergebnis.Gespeichert(ERWARTET));
        assertThat(anwendungsfall.aufrufe).containsExactly(new Aufruf(new BenutzerKennung("erika"),
                new SnapshotId(vorschauVonErika)));
    }

    @Test
    void eineWiederholungErhaeltDasBereitsGespeicherteSchreiben() {
        anwendungsfall.ergebnis = () ->
                new dieterbaier.standardschreiben.usecases.in.Speicherergebnis.BereitsGespeichert(ausDerVorschau);

        var ergebnis = api.speichere("erika", vorschauVonErika);

        assertThat(ergebnis).isEqualTo(new Speicherergebnis.BereitsGespeichert(ERWARTET));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"SnapshotUnbekannt", "SnapshotAbgelaufen", "NichtErlaubt", "DatenkontextGeaendert",
        "NichtKonform"})
    void eineFachlicheAblehnungKommtAlsErgebnisZurueck(String ablehnung) {
        anwendungsfall.ergebnis = () -> ablehnungDesAnwendungsfalls(ablehnung);

        var ergebnis = api.speichere("erika", vorschauVonErika);

        assertThat(ergebnis).isEqualTo(switch (ablehnung) {
            case "SnapshotUnbekannt" -> new Speicherergebnis.SnapshotUnbekannt();
            case "SnapshotAbgelaufen" -> new Speicherergebnis.SnapshotAbgelaufen();
            case "NichtErlaubt" -> new Speicherergebnis.NichtErlaubt();
            case "DatenkontextGeaendert" -> new Speicherergebnis.DatenkontextGeaendert();
            default -> new Speicherergebnis.NichtKonform(List.of("PDF/UA-1: Bild ohne Alternativtext"));
        });
    }

    @Test
    void dasSpeichernTrittDerTransaktionDesHostsBei() {
        host.executeWithoutResult(status -> api.speichere("erika", vorschauVonErika));

        assertThat(transaktionen.liefBeimAufruf).as("in der Transaktion des Hosts").isTrue();
        assertThat(transaktionen.eroeffnetBeimAufruf).as("keine weitere Transaktion").isEqualTo(1);
        assertThat(transaktionen.eroeffnet).isEqualTo(1);
        assertThat(transaktionen.committet).isEqualTo(1);
    }

    @Test
    void ohneTransaktionDesHostsEroeffnetDasSpeichernEineEigene() {
        api.speichere("erika", vorschauVonErika);

        assertThat(transaktionen.liefBeimAufruf).isTrue();
        assertThat(transaktionen.eroeffnet).isEqualTo(1);
        assertThat(transaktionen.committet).isEqualTo(1);
        assertThat(transaktionen.zurueckgerollt).isZero();
    }

    @Test
    void eineFachlicheAblehnungLaesstDieTransaktionDesHostsCommitten() {
        anwendungsfall.ergebnis = () -> ablehnungDesAnwendungsfalls("NichtErlaubt");

        var ergebnis = host.execute(status -> api.speichere("erika", vorschauVonErika));

        assertThat(ergebnis).isEqualTo(new Speicherergebnis.NichtErlaubt());
        assertThat(transaktionen.nurNochRollback).as("nicht rollback-only").isFalse();
        assertThat(transaktionen.committet).isEqualTo(1);
        assertThat(transaktionen.zurueckgerollt).isZero();
    }

    @Test
    void einTechnischerFehlerRolltDieTransaktionDesHostsZurueck() {
        anwendungsfall.ergebnis = () -> {
            throw new IllegalStateException("veraPDF nicht erreichbar");
        };
        List<RuntimeException> fehlerBeimHost = new ArrayList<>();

        // Der Host fängt den Fehler und versucht trotzdem zu committen.
        assertThatThrownBy(() -> host.executeWithoutResult(status -> {
            try {
                api.speichere("erika", vorschauVonErika);
            } catch (IllegalStateException fehler) {
                fehlerBeimHost.add(fehler);
            }
        })).isInstanceOf(UnexpectedRollbackException.class);

        assertThat(fehlerBeimHost).singleElement().extracting(Throwable::getMessage)
                .isEqualTo("veraPDF nicht erreichbar");
        assertThat(transaktionen.nurNochRollback).isTrue();
        assertThat(transaktionen.committet).isZero();
        assertThat(transaktionen.zurueckgerollt).isEqualTo(1);
    }

    @Test
    void rolltDerHostZurueckRolltDasSpeichernMitZurueck() {
        var ergebnis = host.execute(status -> {
            Speicherergebnis gespeichert = api.speichere("erika", vorschauVonErika);
            status.setRollbackOnly();
            return gespeichert;
        });

        assertThat(ergebnis).isInstanceOf(Speicherergebnis.Gespeichert.class);
        assertThat(transaktionen.eroeffnet).isEqualTo(1);
        assertThat(transaktionen.committet).isZero();
        assertThat(transaktionen.zurueckgerollt).isEqualTo(1);
    }

    @Test
    void eineUngueltigeBenutzerkennungRolltDieTransaktionDesHostsZurueck() {
        List<RuntimeException> fehlerBeimHost = new ArrayList<>();

        // Der Host fängt den Fehler und versucht trotzdem zu committen.
        assertThatThrownBy(() -> host.executeWithoutResult(status -> {
            try {
                api.speichere(" ", vorschauVonErika);
            } catch (IllegalArgumentException fehler) {
                fehlerBeimHost.add(fehler);
            }
        })).isInstanceOf(UnexpectedRollbackException.class);

        assertThat(fehlerBeimHost).singleElement().extracting(Throwable::getMessage).asString()
                .contains("Benutzerkennung");
        assertThat(anwendungsfall.aufrufe).isEmpty();
        assertThat(transaktionen.nurNochRollback).isTrue();
        assertThat(transaktionen.committet).isZero();
        assertThat(transaktionen.zurueckgerollt).isEqualTo(1);
    }

    /** Kein Szenario: Die Java-API ist ohne Rückruf verdrahtet (ADR-023). Kommt die Ablehnung doch, rollt sie zurück. */
    @Test
    void eineAblehnungDurchDenRueckrufIstEinFehlerDerVerdrahtung() {
        anwendungsfall.ergebnis = () -> new dieterbaier.standardschreiben.usecases.in.Speicherergebnis
                .VomHostAbgelehnt("Die Aufgabe ist bereits erledigt");

        assertThatThrownBy(() -> host.executeWithoutResult(status -> api.speichere("erika", vorschauVonErika)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("keinen Rückruf");
        assertThat(transaktionen.committet).isZero();
        assertThat(transaktionen.zurueckgerollt).isEqualTo(1);
    }

    private static dieterbaier.standardschreiben.usecases.in.Speicherergebnis ablehnungDesAnwendungsfalls(
            String ablehnung) {
        return switch (ablehnung) {
            case "SnapshotUnbekannt" -> new dieterbaier.standardschreiben.usecases.in.Speicherergebnis
                    .SnapshotUnbekannt();
            case "SnapshotAbgelaufen" -> new dieterbaier.standardschreiben.usecases.in.Speicherergebnis
                    .SnapshotAbgelaufen();
            case "NichtErlaubt" -> new dieterbaier.standardschreiben.usecases.in.Speicherergebnis.NichtErlaubt();
            case "DatenkontextGeaendert" -> new dieterbaier.standardschreiben.usecases.in.Speicherergebnis
                    .DatenkontextGeaendert();
            case "NichtKonform" -> new dieterbaier.standardschreiben.usecases.in.Speicherergebnis.NichtKonform(
                    List.of("PDF/UA-1: Bild ohne Alternativtext"));
            default -> throw new IllegalArgumentException(ablehnung);
        };
    }

    private record Aufruf(BenutzerKennung benutzer, SnapshotId snapshot) {}

    /** Attrappe des eingehenden Ports; merkt sich die Aufrufe und ob dabei eine Transaktion lief. */
    private static final class Anwendungsfall
            implements dieterbaier.standardschreiben.usecases.in.SchreibenSpeichern {

        private final Transaktionsattrappe transaktionen;
        final List<Aufruf> aufrufe = new ArrayList<>();
        Supplier<dieterbaier.standardschreiben.usecases.in.Speicherergebnis> ergebnis = () -> {
            throw new IllegalStateException("kein Ergebnis vorbereitet");
        };

        Anwendungsfall(Transaktionsattrappe transaktionen) {
            this.transaktionen = transaktionen;
        }

        @Override
        public dieterbaier.standardschreiben.usecases.in.Speicherergebnis speichere(
                BenutzerKennung benutzer, SnapshotId snapshot) {
            aufrufe.add(new Aufruf(benutzer, snapshot));
            transaktionen.merkeAufruf();
            return ergebnis.get();
        }
    }
}
