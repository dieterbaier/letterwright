package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Briefpapier;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Vorlagenversion;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.DokumentAblage;
import dieterbaier.standardschreiben.usecases.out.Konformitaet;
import dieterbaier.standardschreiben.usecases.out.KonformitaetsPruefer;
import dieterbaier.standardschreiben.usecases.out.Platzhalterdatenquelle;
import dieterbaier.standardschreiben.usecases.out.SchreibenRepository;
import dieterbaier.standardschreiben.usecases.out.Snapshotablage;
import dieterbaier.standardschreiben.usecases.out.VorlagenRepository;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** Attrappen der ausgehenden Ports für die Tests der Anwendungsfälle. Sie halten fest, was gefragt wurde. */
final class Attrappen {

    private Attrappen() {
    }

    static final class Vorlagen implements VorlagenRepository {

        final Map<VorlagenId, Standardschreiben> standardschreiben = new HashMap<>();
        final Map<VorlagenId, Briefpapier> briefpapiere = new HashMap<>();

        Standardschreiben ablegen(Standardschreiben vorlage) {
            standardschreiben.put(vorlage.id(), vorlage);
            return vorlage;
        }

        Briefpapier ablegen(Briefpapier vorlage) {
            briefpapiere.put(vorlage.id(), vorlage);
            return vorlage;
        }

        @Override
        public Optional<Standardschreiben> standardschreiben(VorlagenId id) {
            return Optional.ofNullable(standardschreiben.get(id));
        }

        @Override
        public Optional<Briefpapier> briefpapier(VorlagenId id) {
            return Optional.ofNullable(briefpapiere.get(id));
        }
    }

    static final class Host implements Berechtigungen, Platzhalterdatenquelle {

        Datenkontext datenkontext = Datenkontext.aus(Map.of("name", "Erika Mustermann GmbH"));
        final List<Standardschreiben> erzeugenAngefragt = new ArrayList<>();
        boolean erlaubt;
        int datenkontextAngefragt;

        boolean wurdeGefragt() {
            return !erzeugenAngefragt.isEmpty() || datenkontextAngefragt > 0;
        }

        @Override
        public boolean darf(BenutzerKennung benutzer, VorlagenAktion aktion) {
            throw new AssertionError("Vorlagenaktionen gehören nicht zur Vorschau");
        }

        @Override
        public boolean darf(BenutzerKennung benutzer, SchreibenAktion aktion, FachlicherBezug bezug) {
            throw new AssertionError("Aktionen an gespeicherten Schreiben gehören nicht zur Vorschau");
        }

        @Override
        public boolean darfErzeugen(BenutzerKennung benutzer, FachlicherBezug bezug, Standardschreiben vorlage) {
            erzeugenAngefragt.add(vorlage);
            return erlaubt;
        }

        @Override
        public Datenkontext liesDatenkontext(BenutzerKennung benutzer, FachlicherBezug bezug, Standardschreiben vorlage) {
            datenkontextAngefragt++;
            return datenkontext;
        }
    }

    /** Ersetzt nur {@code {{name}}}; wie wirklich zusammengeführt wird, prüft {@code adapter-rendering}. */
    static final class Renderer implements dieterbaier.standardschreiben.usecases.out.Renderer {

        int aufrufe;
        int pdfs;

        @Override
        public String zusammenfuehren(Vorlagenversion briefpapier, Vorlagenversion inhalt, Datenkontext daten) {
            aufrufe++;
            return briefpapier.inhalt() + inhalt.inhalt().replace("{{name}}", String.valueOf(daten.werte().get("name")));
        }

        /** Das „PDF“ ist das HTML als Bytes; so sieht ein Test, aus welchen Versionen und Daten es entstand. */
        @Override
        public byte[] erzeugePdf(Vorlagenversion briefpapier, Vorlagenversion inhalt, Datenkontext daten) {
            pdfs++;
            return zusammenfuehren(briefpapier, inhalt, daten).getBytes(StandardCharsets.UTF_8);
        }
    }

    static final class Snapshots implements Snapshotablage {

        final Map<SnapshotId, Snapshot> snapshots = new HashMap<>();
        final List<SnapshotId> geloescht = new ArrayList<>();

        @Override
        public void speichere(Snapshot snapshot) {
            snapshots.put(snapshot.id(), snapshot);
        }

        @Override
        public Optional<Snapshot> lade(SnapshotId id) {
            return Optional.ofNullable(snapshots.get(id));
        }

        @Override
        public void loesche(SnapshotId id) {
            snapshots.remove(id);
            geloescht.add(id);
        }
    }

    static final class Uhr extends Clock {

        Instant jetzt;

        Uhr(Instant jetzt) {
            this.jetzt = jetzt;
        }

        @Override
        public Instant instant() {
            return jetzt;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            throw new UnsupportedOperationException();
        }
    }

    /** Ein konfigurierbares Ergebnis; ohne Vorgabe ist jedes PDF konform. */
    static final class Pruefer implements KonformitaetsPruefer {

        Function<byte[], Konformitaet> ergebnis = pdf -> Konformitaet.konform();

        @Override
        public Konformitaet pruefe(byte[] pdf) {
            return ergebnis.apply(pdf);
        }
    }

    static final class Dokumente implements DokumentAblage {

        final Map<Dokumentverweis, byte[]> abgelegt = new LinkedHashMap<>();
        int nummer;

        @Override
        public Dokumentverweis lege(byte[] pdf) {
            var verweis = new Dokumentverweis("ablage/" + ++nummer);
            abgelegt.put(verweis, pdf);
            return verweis;
        }

        @Override
        public void entferne(Dokumentverweis dokument) {
            abgelegt.remove(dokument);
        }
    }

    /**
     * Hält die Eindeutigkeit je Snapshot wie die Persistenz. {@link #zuvor} simuliert eine gleichzeitige Anfrage, die
     * zwischen Suche und Speichern ein Schreiben zum selben Snapshot ablegt.
     */
    static final class SchreibenAblage implements SchreibenRepository {

        final Map<SnapshotId, Schreiben> gespeichert = new HashMap<>();
        Optional<Schreiben> zuvor = Optional.empty();
        Optional<RuntimeException> fehler = Optional.empty();

        @Override
        public Optional<Schreiben> zuSnapshot(SnapshotId snapshot) {
            return Optional.ofNullable(gespeichert.get(snapshot));
        }

        @Override
        public Schreiben speichereEinmalig(Schreiben schreiben) {
            fehler.ifPresent(f -> {
                throw f;
            });
            zuvor.ifPresent(gleichzeitig -> gespeichert.put(gleichzeitig.snapshot(), gleichzeitig));
            return gespeichert.computeIfAbsent(schreiben.snapshot(), snapshot -> schreiben);
        }
    }
}
