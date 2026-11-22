package dieterbaier.standardschreiben.starter;

import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.VorlagenAktion;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Rollen der Default-{@link dieterbaier.standardschreiben.spi.Berechtigungspruefung}. Ohne Eintrag gilt die Authority
 * {@code ROLE_SSW_VORLAGEN_<AKTION>} oder {@code ROLE_SSW_SCHREIBEN_<AKTION>}, etwa {@code ROLE_SSW_SCHREIBEN_ERZEUGEN};
 * ein Host vergibt sie in Spring Security mit {@code roles("SSW_SCHREIBEN_ERZEUGEN")}. Abweichend:
 *
 * <pre>standardschreiben.berechtigung.rollen.schreiben.ERZEUGEN=ROLE_SACHBEARBEITUNG</pre>
 *
 * @param vorlagen die Authority je Aktion an Vorlagen, wo sie vom Default abweicht
 * @param schreiben die Authority je Rolle an Schreiben, wo sie vom Default abweicht
 */
@ConfigurationProperties("standardschreiben.berechtigung.rollen")
record Berechtigungseigenschaften(
        @DefaultValue Map<VorlagenAktion, String> vorlagen, @DefaultValue Map<SchreibenRolle, String> schreiben) {

    private static final String PRAEFIX = "standardschreiben.berechtigung.rollen.";

    Berechtigungseigenschaften {
        vorlagen = geprueft(vorlagen, PRAEFIX + "vorlagen.");
        schreiben = geprueft(schreiben, PRAEFIX + "schreiben.");
    }

    /** Die Authority, die ein Benutzer für die Anfrage braucht. */
    String rolle(Berechtigungsanfrage anfrage) {
        return switch (anfrage) {
            case Berechtigungsanfrage.ZuVorlagen zuVorlagen -> vorlagen.getOrDefault(
                    zuVorlagen.aktion(), "ROLE_SSW_VORLAGEN_" + zuVorlagen.aktion().name());
            case Berechtigungsanfrage.ZuSchreiben zuSchreiben -> rolle(switch (zuSchreiben.aktion()) {
                case ANSEHEN -> SchreibenRolle.ANSEHEN;
                case HERUNTERLADEN -> SchreibenRolle.HERUNTERLADEN;
                case LOESCHEN -> SchreibenRolle.LOESCHEN;
            });
            case Berechtigungsanfrage.ZumErzeugen zumErzeugen -> rolle(SchreibenRolle.ERZEUGEN);
        };
    }

    private String rolle(SchreibenRolle rolle) {
        return schreiben.getOrDefault(rolle, "ROLE_SSW_SCHREIBEN_" + rolle.name());
    }

    private static <A extends Enum<A>> Map<A, String> geprueft(Map<A, String> rollen, String schluessel) {
        rollen.forEach((aktion, rolle) -> {
            if (rolle.isBlank()) {
                throw new IllegalArgumentException(schluessel + aktion.name() + " darf nicht leer sein");
            }
        });
        return rollen.isEmpty() ? Map.of() : Map.copyOf(new EnumMap<>(rollen));
    }
}
