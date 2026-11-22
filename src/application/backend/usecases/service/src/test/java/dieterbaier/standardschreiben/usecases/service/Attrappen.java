package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.Seite;
import dieterbaier.standardschreiben.domaene.modell.Seitenanfrage;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Loeschentscheidung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenAusSnapshot;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshotzustand;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Briefpapier;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Vorlagenversion;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.DokumentAblage;
import dieterbaier.standardschreiben.usecases.out.Ersetzungsausgang;
import dieterbaier.standardschreiben.usecases.out.Konformitaet;
import dieterbaier.standardschreiben.usecases.out.KonformitaetsPruefer;
import dieterbaier.standardschreiben.usecases.out.Loeschregeln;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

        /** Läuft unmittelbar vor dem bedingten Ersetzen, etwa als gleichzeitige Änderung eines anderen Benutzers. */
        Runnable zuvor = () -> { };
        int ersetzungen;

        @Override
        public Optional<Briefpapier> briefpapier(VorlagenId id) {
            return Optional.ofNullable(briefpapiere.get(id));
        }

        @Override
        public Ersetzungsausgang ersetze(Standardschreiben geladen, Standardschreiben geaendert) {
            zuvor.run();
            Standardschreiben gespeichert = standardschreiben.get(geladen.id());
            if (gespeichert == null || !stand(gespeichert.versionen()).equals(stand(geladen.versionen()))) {
                return new Ersetzungsausgang.Veraltet();
            }
            ersetzungen++;
            ablegen(geaendert);
            return new Ersetzungsausgang.Ersetzt();
        }

        @Override
        public Ersetzungsausgang ersetze(Briefpapier geladen, Briefpapier geaendert) {
            zuvor.run();
            Briefpapier gespeichert = briefpapiere.get(geladen.id());
            if (gespeichert == null || !stand(gespeichert.versionen()).equals(stand(geladen.versionen()))) {
                return new Ersetzungsausgang.Veraltet();
            }
            ersetzungen++;
            ablegen(geaendert);
            return new Ersetzungsausgang.Ersetzt();
        }

        /** Vorlagenversionen sind über ihren Verweis gleich; der Vergleich braucht den ganzen Stand. */
        private static List<List<Object>> stand(List<Vorlagenversion> versionen) {
            return versionen.stream()
                    .map(version -> List.<Object>of(version.verweis(), version.zustand(), version.inhalt(),
                            version.beispieldaten()))
                    .toList();
        }
    }

    static final class Host implements Berechtigungen, Platzhalterdatenquelle {

        Datenkontext datenkontext = Datenkontext.aus(Map.of("name", "Erika Mustermann GmbH"));
        final List<Standardschreiben> erzeugenAngefragt = new ArrayList<>();
        final List<Erlaubnis> schreibenAktionAngefragt = new ArrayList<>();
        final Set<Erlaubnis> schreibenAktionen = new java.util.HashSet<>();
        final Set<BenutzerKennung> duerfenVeroeffentlichen = new java.util.HashSet<>();
        final List<VorlagenAktion> vorlagenAktionAngefragt = new ArrayList<>();
        boolean erlaubt;
        int datenkontextAngefragt;

        boolean wurdeGefragt() {
            return !erzeugenAngefragt.isEmpty() || datenkontextAngefragt > 0 || !schreibenAktionAngefragt.isEmpty()
                    || !vorlagenAktionAngefragt.isEmpty();
        }

        void erlaube(BenutzerKennung benutzer, SchreibenAktion aktion, FachlicherBezug bezug) {
            schreibenAktionen.add(new Erlaubnis(benutzer, aktion, bezug));
        }

        void verbiete(BenutzerKennung benutzer, SchreibenAktion aktion, FachlicherBezug bezug) {
            schreibenAktionen.remove(new Erlaubnis(benutzer, aktion, bezug));
        }

        /** Erlaubt ist nur das Veröffentlichen, und nur den Benutzern in {@link #duerfenVeroeffentlichen}. */
        @Override
        public boolean darf(BenutzerKennung benutzer, VorlagenAktion aktion) {
            vorlagenAktionAngefragt.add(aktion);
            return aktion == VorlagenAktion.VEROEFFENTLICHEN && duerfenVeroeffentlichen.contains(benutzer);
        }

        /** Erlaubt ist, was {@link #erlaube} freigegeben hat. */
        @Override
        public boolean darf(BenutzerKennung benutzer, SchreibenAktion aktion, FachlicherBezug bezug) {
            Erlaubnis anfrage = new Erlaubnis(benutzer, aktion, bezug);
            schreibenAktionAngefragt.add(anfrage);
            return schreibenAktionen.contains(anfrage);
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

    record Erlaubnis(BenutzerKennung benutzer, SchreibenAktion aktion, FachlicherBezug bezug) {}

    /** Antwortet mit der vorgegebenen Entscheidung und hält fest, wonach gefragt wurde. */
    static final class Loeschrichtlinie implements Loeschregeln {

        Loeschentscheidung entscheidung = Loeschentscheidung.hart();
        final List<Schreiben> angefragt = new ArrayList<>();

        @Override
        public Loeschentscheidung entscheide(BenutzerKennung benutzer, Schreiben schreiben) {
            angefragt.add(schreiben);
            return entscheidung;
        }
    }

    /** Ersetzt nur {@code {{name}}}; wie wirklich zusammengeführt wird, prüft {@code adapter-rendering}. */
    static final class Renderer implements dieterbaier.standardschreiben.usecases.out.Renderer {

        int aufrufe;
        int pdfs;
        final List<Datenkontext> pdfDaten = new ArrayList<>();

        @Override
        public String zusammenfuehren(Vorlagenversion briefpapier, Vorlagenversion inhalt, Datenkontext daten) {
            aufrufe++;
            return briefpapier.inhalt() + inhalt.inhalt().replace("{{name}}", String.valueOf(daten.werte().get("name")));
        }

        /** Das „PDF“ ist das HTML als Bytes; so sieht ein Test, aus welchen Versionen und Daten es entstand. */
        @Override
        public byte[] erzeugePdf(Vorlagenversion briefpapier, Vorlagenversion inhalt, Datenkontext daten) {
            pdfs++;
            pdfDaten.add(daten);
            return zusammenfuehren(briefpapier, inhalt, daten).getBytes(StandardCharsets.UTF_8);
        }

        /** Wie {@link #erzeugePdf}, mit „Beispielinhalt“ an der Stelle des Standardschreibens. */
        @Override
        public byte[] erzeugeProbePdf(Vorlagenversion briefpapier, Datenkontext beispieldaten) {
            pdfs++;
            pdfDaten.add(beispieldaten);
            return (briefpapier.inhalt() + "Beispielinhalt").getBytes(StandardCharsets.UTF_8);
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
        int gelesen;

        @Override
        public Dokumentverweis lege(byte[] pdf) {
            var verweis = new Dokumentverweis("ablage/" + ++nummer);
            abgelegt.put(verweis, pdf);
            return verweis;
        }

        @Override
        public byte[] lies(Dokumentverweis dokument) {
            gelesen++;
            byte[] pdf = abgelegt.get(dokument);
            if (pdf == null) {
                throw new IllegalStateException("Kein PDF zu " + dokument.wert());
            }
            return pdf.clone();
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
        int abfragen;

        SchreibenAblage(Snapshots snapshots) {
            this.snapshots = snapshots;
        }

        /** Legt ein Schreiben ab, als wäre es früher gespeichert worden. */
        Schreiben ablegen(Schreiben schreiben) {
            gespeichert.put(schreiben.snapshot(), schreiben);
            return schreiben;
        }

        Optional<Schreiben> mitId(SchreibenId id) {
            return gespeichert.values().stream().filter(schreiben -> schreiben.id().equals(id)).findFirst();
        }

        @Override
        public Optional<Schreiben> lade(SchreibenId id) {
            return mitId(id);
        }

        @Override
        public Optional<Schreiben> zuSnapshot(SnapshotId snapshot) {
            return Optional.ofNullable(gespeichert.get(snapshot));
        }

        /** Filtert wie die Datenbankabfrage vor dem Paginieren und Zählen. */
        @Override
        public Seite<Schreiben> sichtbareZu(FachlicherBezug bezug, Seitenanfrage anfrage) {
            abfragen++;
            List<Schreiben> sichtbare = gespeichert.values().stream()
                    .filter(schreiben -> schreiben.bezug().equals(bezug))
                    .filter(Schreiben::istSichtbar)
                    .sorted(Comparator.comparing(Schreiben::erstellt).reversed())
                    .toList();
            return new Seite<>(sichtbare.stream().skip(anfrage.versatz()).limit(anfrage.groesse()).toList(),
                    sichtbare.size(), anfrage);
        }

        @Override
        public void ersetze(Schreiben geaendert) {
            gespeichert.put(geaendert.snapshot(), geaendert);
        }

        @Override
        public void entferne(SchreibenId id) {
            mitId(id).ifPresent(schreiben -> gespeichert.remove(schreiben.snapshot()));
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
            if (aktuell == null) {
                return new Speicherausgang.SnapshotNichtMehrVorhanden();
            }
            if (aktuell.zustand() instanceof Snapshotzustand.Verbraucht) {
                Schreiben vorhandenes = gespeichert.get(id);
                return vorhandenes == null
                        ? new Speicherausgang.SchreibenEntfernt()
                        : new Speicherausgang.SnapshotBereitsVerbraucht(vorhandenes);
            }
            gespeichert.put(id, neu.schreiben());
            snapshots.snapshots.put(id, neu.verbrauchterSnapshot());
            return new Speicherausgang.Gespeichert();
        }
    }
}
