package dieterbaier.standardschreiben.domaene.modell.vorlage;

import dieterbaier.standardschreiben.validierung.Pruefe;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
 * </ul>
 */
final class Versionsfolge {

    private final VorlagenId vorlage;
    private final List<Vorlagenversion> versionen;

    private Versionsfolge(VorlagenId vorlage, List<Vorlagenversion> versionen) {
        this.vorlage = vorlage;
        this.versionen = List.copyOf(versionen);
    }

    static Versionsfolge erste(VorlagenId vorlage, String inhalt) {
        return new Versionsfolge(vorlage, List.of(Vorlagenversion.entwurf(vorlage, Versionsnummer.ERSTE, inhalt)));
    }

    static Versionsfolge wiederherstellen(VorlagenId vorlage, List<Vorlagenversion> versionen) {
        Pruefe.pflichtOhneLuecken(versionen, "Die Versionen");
        if (versionen.isEmpty()) {
            throw new Validierungsfehler("Eine Vorlage hat mindestens eine Version");
        }
        List<Vorlagenversion> sortiert = new ArrayList<>(versionen);
        sortiert.sort(Comparator.comparing(Vorlagenversion::nummer));
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

    Optional<Vorlagenversion> neuesteVeroeffentlichte() {
        return versionen.reversed().stream()
                .filter(version -> version.zustand() == Vorlagenzustand.VEROEFFENTLICHT)
                .findFirst();
    }

    Versionsfolge aendereEntwurf(String inhalt) {
        return ersetze(verlangeEntwurf("Ändern").mitInhalt(inhalt));
    }

    Versionsfolge veroeffentlicheEntwurf() {
        Vorlagenversion entwurf = verlangeEntwurf("Veröffentlichen");
        if (entwurf.inhalt().isBlank()) {
            throw new UnzulaessigerZustandswechsel(vorlage,
                    "Ein Entwurf ohne Inhalt kann nicht veröffentlicht werden");
        }
        return ersetze(entwurf.imZustand(Vorlagenzustand.VEROEFFENTLICHT));
    }

    Versionsfolge neuerEntwurf(String inhalt) {
        entwurf().ifPresent(vorhanden -> {
            throw new UnzulaessigerZustandswechsel(vorlage, "Version " + vorhanden.nummer().wert()
                    + " ist schon ein Entwurf; es gibt höchstens einen");
        });
        Versionsnummer naechste = versionen.getLast().nummer().naechste();
        List<Vorlagenversion> neu = new ArrayList<>(versionen);
        neu.add(Vorlagenversion.entwurf(vorlage, naechste, inhalt));
        return new Versionsfolge(vorlage, neu);
    }

    Versionsfolge zurueckziehen(Versionsnummer nummer) {
        Vorlagenversion version = version(Pruefe.pflicht(nummer, "Die Versionsnummer"))
                .orElseThrow(() -> new Validierungsfehler("Version " + nummer.wert() + " gibt es nicht"));
        if (version.zustand() != Vorlagenzustand.VEROEFFENTLICHT) {
            throw new UnzulaessigerZustandswechsel(vorlage, "Version " + nummer.wert() + " ist " + version.zustand()
                    + "; zurückziehen lässt sich nur eine veröffentlichte Version");
        }
        return ersetze(version.imZustand(Vorlagenzustand.ZURUECKGEZOGEN));
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
