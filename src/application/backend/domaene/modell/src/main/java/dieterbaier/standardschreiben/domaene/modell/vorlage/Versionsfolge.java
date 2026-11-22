package dieterbaier.standardschreiben.domaene.modell.vorlage;

import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.validierung.Pruefe;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;
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
final class Versionsfolge {

    private final VorlagenId vorlage;
    private final List<Vorlagenversion> versionen;

    private Versionsfolge(VorlagenId vorlage, List<Vorlagenversion> versionen) {
        this.vorlage = vorlage;
        this.versionen = List.copyOf(versionen);
    }

    static Versionsfolge erste(VorlagenId vorlage, String inhalt) {
        return new Versionsfolge(vorlage, List.of(
                Vorlagenversion.entwurf(vorlage, Versionsnummer.ERSTE, inhalt, Datenkontext.aus(Map.of()))));
    }

    static Versionsfolge wiederherstellen(VorlagenId vorlage, List<Vorlagenversion> versionen) {
        Pruefe.pflichtOhneLuecken(versionen, "Die Versionen");
        if (versionen.isEmpty()) {
            throw new Validierungsfehler("Eine Vorlage hat mindestens eine Version");
        }
        List<Vorlagenversion> sortiert = new ArrayList<>(versionen);
        sortiert.sort(Comparator.comparing(Vorlagenversion::nummer));
        boolean mitEntwurf = sortiert.getLast().zustand() == Vorlagenzustand.ENTWURF;
        int neuesteFreigegebene = sortiert.size() - (mitEntwurf ? 2 : 1);
        Versionsnummer erwartet = Versionsnummer.ERSTE;
        for (int position = 0; position < sortiert.size(); position++) {
            Vorlagenversion version = sortiert.get(position);
            if (!version.vorlage().equals(vorlage)) {
                throw new Validierungsfehler("Version " + version.nummer().wert() + " gehört zur Vorlage "
                        + version.vorlage().wert() + ", nicht zu " + vorlage.wert());
            }
            if (!version.nummer().equals(erwartet)) {
                throw new Validierungsfehler("Die Versionsnummern laufen nicht lückenlos von 1 an: erwartet "
                        + erwartet.wert() + ", gefunden " + version.nummer().wert());
            }
            boolean istNeueste = position == sortiert.size() - 1;
            if (version.zustand() == Vorlagenzustand.ENTWURF && !istNeueste) {
                throw new Validierungsfehler("Version " + version.nummer().wert()
                        + " ist ein Entwurf, aber nicht die neueste Version");
            }
            if (version.zustand() == Vorlagenzustand.VEROEFFENTLICHT && position != neuesteFreigegebene) {
                throw new Validierungsfehler("Version " + version.nummer().wert() + " ist veröffentlicht, aber nicht "
                        + "die neueste freigegebene Version; je Vorlage ist höchstens eine veröffentlicht");
            }
            if (version.zustand() == Vorlagenzustand.ABGELOEST && position == neuesteFreigegebene) {
                throw new Validierungsfehler("Version " + version.nummer().wert()
                        + " ist abgelöst, aber keine neuere Version ist freigegeben");
            }
            erwartet = erwartet.naechste();
        }
        return new Versionsfolge(vorlage, sortiert);
    }

    List<Vorlagenversion> alle() {
        return versionen;
    }

    Optional<Vorlagenversion> version(Versionsnummer nummer) {
        return versionen.stream().filter(version -> version.nummer().equals(nummer)).findFirst();
    }

    Optional<Vorlagenversion> entwurf() {
        Vorlagenversion neueste = versionen.getLast();
        return neueste.zustand() == Vorlagenzustand.ENTWURF ? Optional.of(neueste) : Optional.empty();
    }

    Optional<Vorlagenversion> veroeffentlichte() {
        return versionen.stream()
                .filter(version -> version.zustand() == Vorlagenzustand.VEROEFFENTLICHT)
                .findFirst();
    }

    Versionsfolge aendereEntwurf(String inhalt) {
        Vorlagenversion entwurf = verlangeEntwurf("Ändern");
        return ersetze(entwurf.mitInhalt(inhalt, entwurf.beispieldaten()));
    }

    Versionsfolge aendereEntwurf(String inhalt, Datenkontext beispieldaten) {
        return ersetze(verlangeEntwurf("Ändern").mitInhalt(inhalt, beispieldaten));
    }

    Versionsfolge veroeffentlicheEntwurf() {
        Vorlagenversion entwurf = verlangeEntwurf("Veröffentlichen");
        if (!entwurf.istVeroeffentlichbar()) {
            throw new UnzulaessigerZustandswechsel(vorlage,
                    "Ein Entwurf ohne Inhalt kann nicht veröffentlicht werden");
        }
        Versionsfolge abgeloest = veroeffentlichte()
                .map(bisherige -> ersetze(bisherige.imZustand(Vorlagenzustand.ABGELOEST)))
                .orElse(this);
        return abgeloest.ersetze(entwurf.imZustand(Vorlagenzustand.VEROEFFENTLICHT));
    }

    Versionsfolge neuerEntwurf(String inhalt) {
        entwurf().ifPresent(vorhanden -> {
            throw new UnzulaessigerZustandswechsel(vorlage, "Version " + vorhanden.nummer().wert()
                    + " ist schon ein Entwurf; es gibt höchstens einen");
        });
        Vorlagenversion neueste = versionen.getLast();
        List<Vorlagenversion> neu = new ArrayList<>(versionen);
        neu.add(Vorlagenversion.entwurf(vorlage, neueste.nummer().naechste(), inhalt, neueste.beispieldaten()));
        return new Versionsfolge(vorlage, neu);
    }

    Versionsfolge zurueckziehen() {
        Vorlagenversion veroeffentlicht = veroeffentlichte().orElseThrow(() -> new UnzulaessigerZustandswechsel(
                vorlage, "Zurückziehen braucht eine veröffentlichte Version; es gibt keine"));
        return ersetze(veroeffentlicht.imZustand(Vorlagenzustand.ZURUECKGEZOGEN));
    }

    private Vorlagenversion verlangeEntwurf(String vorgang) {
        return entwurf().orElseThrow(() -> new UnzulaessigerZustandswechsel(vorlage,
                vorgang + " braucht einen Entwurf; die neueste Version ist " + versionen.getLast().zustand()));
    }

    private Versionsfolge ersetze(Vorlagenversion geaendert) {
        List<Vorlagenversion> neu = new ArrayList<>(versionen);
        neu.replaceAll(version -> version.equals(geaendert) ? geaendert : version);
        return new Versionsfolge(vorlage, neu);
    }
}
