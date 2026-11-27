# language: de
# Status: proposed (KI-gestützt, wartet auf Review). Brücke zu den Tests:
# VorlagenrueckzugTest im Modul usecases-service, ein Test je Szenario,
# benannt nach dem Szenariotitel. Die Ports sind dort durch Attrappen ersetzt.
Funktionalität: Vorlagenversion zurückziehen
  Ein Vorlagenverwalter nimmt die veröffentlichte Version eines Briefpapiers oder
  Standardschreibens ohne Nachfolger zurück. Bis zur nächsten Veröffentlichung
  entsteht mit der Vorlage keine Vorschau und kein Schreiben; gespeicherte
  Schreiben bleiben unverändert (ADR-007, ADR-022). Zurückziehen verlangt dieselbe
  Berechtigung wie Veröffentlichen.

  Grundlage:
    Angenommen die Vorlage hat die veröffentlichte Version 1 und einen Entwurf 2
    Und Vera darf Vorlagen veröffentlichen

  Szenariogrundriss: Die veröffentlichte Version wird ohne Nachfolger zurückgezogen
    Angenommen die Vorlage ist <vorlage>
    Wenn Vera die Vorlage zurückzieht
    Dann ist Version 1 zurückgezogen
    Und die Vorlage hat keine veröffentlichte Version
    Und der Entwurf 2 ist unverändert

    Beispiele:
      | vorlage               |
      | ein Standardschreiben |
      | ein Briefpapier       |

  Szenario: Ohne Berechtigung wird nichts zurückgezogen
    Wenn Max die Vorlage zurückzieht
    Dann ist das Zurückziehen nicht erlaubt
    Und die Vorlage ist unverändert

  Szenariogrundriss: Ohne veröffentlichte Version gibt es nichts zurückzuziehen
    Angenommen die Vorlage ist <vorlage> mit nur einem Entwurf
    Wenn Vera die Vorlage zurückzieht
    Dann ist nichts veröffentlicht

    Beispiele:
      | vorlage               |
      | ein Standardschreiben |
      | ein Briefpapier       |

  Szenario: Eine unbekannte Vorlage wird gemeldet
    Wenn Vera eine Vorlage mit unbekannter ID zurückzieht
    Dann ist die Vorlage unbekannt

  Szenario: Hat ein anderer die Vorlage inzwischen geändert, wird nichts zurückgezogen
    Angenommen ein anderer Vorlagenverwalter veröffentlicht den Entwurf, während Vera zurückzieht
    Wenn Vera die Vorlage zurückzieht
    Dann ist der Stand veraltet
    Und Version 2 bleibt veröffentlicht
