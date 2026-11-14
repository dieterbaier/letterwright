package io.github.dieterbaier.standardschreiben.architektur;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Prüft den Produktionscode aller Backend-Module gegen die Regeln aus ADR-002. */
class HexagonaleArchitekturTest {

    private static final String BASIS = "io.github.dieterbaier.standardschreiben";

    private static JavaClasses produktionsCode;

    @BeforeAll
    static void importiereProduktionsCode() {
        produktionsCode = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(BASIS);
    }

    @Test
    void domaeneIstFrameworkfrei() {
        ArchitekturRegeln.domaeneIstFrameworkfrei(BASIS).check(produktionsCode);
    }

    @Test
    void anwendungKenntKeineAdapter() {
        ArchitekturRegeln.anwendungKenntKeineAdapter(BASIS).check(produktionsCode);
    }

    @Test
    void adapterHaengenNichtVomStarterAb() {
        ArchitekturRegeln.adapterHaengenNichtVomStarterAb(BASIS).check(produktionsCode);
    }
}
