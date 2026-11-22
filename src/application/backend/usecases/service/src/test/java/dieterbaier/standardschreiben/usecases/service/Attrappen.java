package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenAusSnapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshotzustand;
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
import dieterbaier.standardschreiben.usecases.out.Speicherausgang;
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
        /** Läuft vor dem bedingten Löschen und simuliert eine gleichzeitige Anfrage. */
        Runnable vorDemLoeschen = () -> { };

        @Override
        public void speichere(Snapshot snapshot) {
            snapshots.put(snapshot.id(), snapshot);
        }

        @Override
        public Optional<Snapshot> lade(SnapshotId id) {
            return Optional.ofNullable(snapshots.get(id));
        }

        @Override
        public void loescheWennOffen(SnapshotId id) {
            vorDemLoeschen.run();
            Snapshot aktuell = snapshots.get(id);
            if (aktuell != null && aktuell.zustand() instanceof Snapshotzustand.Offen) {
                snapshots.remove(id);
                geloescht.add(id);
            }
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
     * Verhält sich wie die Persistenz: Schreiben speichern und Snapshot verbrauchen geschieht in einem Schritt und nur bei
     * offenem Snapshot. {@link #zuvor} läuft vor diesem Schritt und simuliert eine gleichzeitige Anfrage;
     * {@link #danach} wirft nach dem Speichern, als ginge die Bestätigung verloren.
     */
    static final class SchreibenAblage implements SchreibenRepository {

        final Map<SnapshotId, Schreiben> gespeichert = new HashMap<>();
        private final Snapshots snapshots;
        Runnable zuvor = () -> { };
        Optional<RuntimeException> danach = Optional.empty();

        SchreibenAblage(Snapshots snapshots) {
            this.snapshots = snapshots;
        }

        @Override
        public Optional<Schreiben> zuSnapshot(SnapshotId snapshot) {
            return Optional.ofNullable(gespeichert.get(snapshot));
        }

        @Override
        public Speicherausgang speichere(SchreibenAusSnapshot neu) {
            zuvor.run();
            Speicherausgang ausgang = uebernimm(neu);
            danach.ifPresent(fehler -> {
                throw fehler;
            });
            return ausgang;
        }

        /** Der atomare Schritt selbst, ohne die simulierten Störungen. */
        Speicherausgang uebernimm(SchreibenAusSnapshot neu) {
            SnapshotId id = neu.verbrauchterSnapshot().id();
            Snapshot aktuell = snapshots.snapshots.get(id);
            if (aktuell.zustand() instanceof Snapshotzustand.Verbraucht) {
                return new Speicherausgang.SnapshotBereitsVerbraucht(gespeichert.get(id));
            }
            gespeichert.put(id, neu.schreiben());
            snapshots.snapshots.put(id, neu.verbrauchterSnapshot());
            return new Speicherausgang.Gespeichert();
        }
    }
}
