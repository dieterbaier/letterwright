# letterwright

Embeddable library (Spring Boot starter and React components) for letter
templates: templates for letterheads and letters that business users maintain
themselves, an accessible preview with current domain data, and stored PDF/A
documents for download. Background: issue #1.

**Architecture documentation:** https://dieterbaier.github.io/standardschreiben/

## Language

The base language of the domain is **German**. The library comes from German
correspondence (DIN 5008, form A and B), and the domain experts speak German.
Code, features and documentation are English so that host developers and
contributors without German can read them (ADR-024). The glossary
(`src/docs/arc42/doc-12000-glossary.adoc`) names the German term for every
domain term next to the binding English term in the code. Some of the English
terms are translations that do not fit perfectly; *letter template* for
*Standardschreiben* is the most obvious one.

The architecture documentation was written in German and translated to English
with #85. Artifact IDs, anchors and file names kept their German-derived names
so that links stay stable, for example `adr-021-hoechstens-ein-schreiben-je-snapshot`.

## Status

Architecture draft and early implementation. The domain model, the use cases,
the SPI to the host application and the Java API exist and are tested against
fakes of the ports. Nothing runs end to end yet: persistence (#6), rendering
with conformance checks (#7) and REST plus the wiring in the starter (#10) are
missing. There is no release.

## Architecture documentation

The documentation follows the
[architecture-knowledge-toolkit](https://github.com/docs-as-code-toolkit/architecture-knowledge-toolkit),
which is included as a Git submodule.

```sh
git submodule update --init
./build.sh validate      # check metadata (Docker/Podman or DOCS_TOOLBOX_LOCAL=1)
./build.sh generate      # generate indexes and traceability
./build.sh build         # HTML under build/architecture
```

Entry point: `src/docs/doc-001-arc42.adoc`. The pipeline
`.github/workflows/architekturdoku.yml` publishes the state of `main` on GitHub
Pages. ADRs are under `src/docs/arc42/09-architecture-decisions/`, quality
scenarios under `src/docs/arc42/10-quality-requirements/`, risks under
`src/docs/arc42/11-risks-and-technical-debt/`.

Notes for AI agents are in `AGENTS.md`.

## Building the backend

```sh
./gradlew build   # compiles, tests, checks coverage (≥ 80 % lines and branches per module) and the ArchUnit rules
```

The modules are under `src/application/backend/`, the shared conventions in the
convention plugin under `gradle/build-logic/`, the versions in
`gradle/libs.versions.toml`. The behaviour is specified in Gherkin under
`features/`; each scenario has a test of the same name.

## Building the frontend

See `src/application/frontend/README.adoc`.

## License

MIT
