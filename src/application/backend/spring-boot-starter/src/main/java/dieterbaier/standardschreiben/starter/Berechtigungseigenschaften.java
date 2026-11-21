package dieterbaier.standardschreiben.starter;

import dieterbaier.standardschreiben.spi.Aktion;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Rollen der Default-{@link dieterbaier.standardschreiben.spi.Berechtigungspruefung}. Ohne Eintrag gilt für eine Aktion die
 * Authority {@code ROLE_SSW_<AKTION>}, etwa {@code ROLE_SSW_SCHREIBEN_ERZEUGEN}; ein Host vergibt sie in Spring Security mit
 * {@code roles("SSW_SCHREIBEN_ERZEUGEN")}. Abweichend:
 *
 * <pre>standardschreiben.berechtigung.rollen.SCHREIBEN_ERZEUGEN=ROLE_SACHBEARBEITUNG</pre>
 *
 * @param rollen die Authority je Aktion, wo sie vom Default abweicht
 */
@ConfigurationProperties("standardschreiben.berechtigung")
record Berechtigungseigenschaften(@DefaultValue Map<Aktion, String> rollen) {

    Berechtigungseigenschaften {
        rollen = rollen.isEmpty() ? Map.of() : Map.copyOf(new EnumMap<>(rollen));
        rollen.forEach((aktion, rolle) -> {
            if (rolle.isBlank()) {
                throw new IllegalArgumentException("standardschreiben.berechtigung.rollen." + aktion
                        + " darf nicht leer sein");
            }
        });
    }

    /** Die Authority, die ein Benutzer für die Aktion braucht. */
    String rolle(Aktion aktion) {
        return rollen.getOrDefault(aktion, "ROLE_SSW_" + aktion.name());
    }
}
