package dieterbaier.standardschreiben.usecases.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

/**
 * Die ausgehenden Ports sind reine Schnittstellen in Domänenbegriffen: keine Default-Methoden, keine statische Logik.
 * Kommt doch Logik hinzu, greift das Abdeckungs-Gate dieses Moduls, weil es Testausführungsdaten gibt.
 */
class PortsTest {

    private static final String MODELL = "dieterbaier.standardschreiben.domaene.modell";

    static final List<Class<?>> PORTS = List.of(Platzhalterdatenquelle.class, Berechtigungen.class, Loeschregeln.class,
            Speicherrueckruf.class);

    @ParameterizedTest
    @FieldSource("PORTS")
    void einPortIstEineSchnittstelleOhneLogik(Class<?> port) {
        assertThat(port).isInterface();
        assertThat(Arrays.stream(port.getDeclaredMethods()).filter(PortsTest::hatRumpf)).isEmpty();
    }

    @ParameterizedTest
    @FieldSource("PORTS")
    void einPortKenntNurDomaenentypen(Class<?> port) {
        assertThat(Arrays.stream(port.getDeclaredMethods())
                .flatMap(methode -> java.util.stream.Stream.concat(
                        Arrays.stream(methode.getParameterTypes()), java.util.stream.Stream.of(methode.getReturnType())))
                .filter(typ -> !typ.isPrimitive())
                .map(Class::getPackageName))
                .allMatch(paket -> paket.equals(MODELL) || paket.startsWith(MODELL + "."));
    }

    private static boolean hatRumpf(Method methode) {
        return methode.isDefault() || java.lang.reflect.Modifier.isStatic(methode.getModifiers());
    }
}
