package dieterbaier.letterwright.usecases.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

/**
 * The outbound ports are pure interfaces in domain terms: no default methods, no static logic. If logic is added after
 * all, the coverage gate of this module applies, because there is test execution data.
 */
class PortsTest {

    private static final String MODEL = "dieterbaier.letterwright.domain.model";

    static final List<Class<?>> PORTS = List.of(PlaceholderDataSource.class, Permissions.class, DeletionRules.class,
            SaveCallback.class);

    @ParameterizedTest
    @FieldSource("PORTS")
    void aPortIsAnInterfaceWithoutLogic(Class<?> port) {
        assertThat(port).isInterface();
        assertThat(Arrays.stream(port.getDeclaredMethods()).filter(PortsTest::hasBody)).isEmpty();
    }

    @ParameterizedTest
    @FieldSource("PORTS")
    void aPortKnowsOnlyDomainTypes(Class<?> port) {
        assertThat(Arrays.stream(port.getDeclaredMethods())
                .flatMap(method -> java.util.stream.Stream.concat(
                        Arrays.stream(method.getParameterTypes()), java.util.stream.Stream.of(method.getReturnType())))
                .filter(type -> !type.isPrimitive())
                .map(Class::getPackageName))
                .allMatch(pkg -> pkg.equals(MODEL) || pkg.startsWith(MODEL + "."));
    }

    private static boolean hasBody(Method method) {
        return method.isDefault() || java.lang.reflect.Modifier.isStatic(method.getModifiers());
    }
}
