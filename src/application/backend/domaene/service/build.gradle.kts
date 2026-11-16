plugins {
    id("standardschreiben.java-modul")
}

description = "Domänenservices: fachliche Regeln über mehrere Modellobjekte. Frei von Frameworks, beim Kompilieren nur für usecases-service sichtbar (ADR-015)."

dependencies {
    api(project(":domaene-modell"))
}
