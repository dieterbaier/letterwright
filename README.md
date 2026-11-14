# standardschreiben

Einbettbare Bibliothek (Spring-Boot-Starter und React-Komponenten) für
Standardschreiben: Vorlagen für Briefpapier und Schreiben, die Fachanwender
pflegen, eine barrierefreie Vorschau mit aktuellen Domänendaten und
gespeicherte PDF/A-Dokumente zum Download. Hintergrund: Issue #1.

## Status

Architekturentwurf. Es gibt noch keinen Anwendungscode.

## Architekturdokumentation

Die Doku folgt dem
[architecture-knowledge-toolkit](https://github.com/docs-as-code-toolkit/architecture-knowledge-toolkit),
das als Git-Submodule eingebunden ist.

```sh
git submodule update --init
./build.sh validate      # Metadaten prüfen (Docker/Podman oder DOCS_TOOLBOX_LOCAL=1)
./build.sh generate      # Indizes und Traceability erzeugen
./build.sh build         # HTML unter build/architecture
```

Einstieg: `src/docs/doc-001-arc42.adoc`. Der Stand von `main` wird von der
Pipeline `.github/workflows/architekturdoku.yml` auf GitHub Pages veröffentlicht. ADRs liegen unter
`src/docs/arc42/09-architecture-decisions/`, Qualitätsszenarien unter
`src/docs/arc42/10-quality-requirements/`, Risiken unter
`src/docs/arc42/11-risks-and-technical-debt/`.

Hinweise für KI-Agenten stehen in `AGENTS.md`.

## Lizenz

MIT
