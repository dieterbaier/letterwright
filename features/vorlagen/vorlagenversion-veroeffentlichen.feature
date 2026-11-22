# language: de
# Status: proposed (KI-gestützt, wartet auf Review). Brücke zu den Tests:
# VorlagenveroeffentlichungTest im Modul usecases-service, ein Test je Szenario,
# benannt nach dem Szenariotitel. Die Ports sind dort durch Attrappen ersetzt.
Funktionalität: Vorlagenversion veröffentlichen
  Ein Vorlagenverwalter veröffentlicht den Entwurf eines Briefpapiers oder
  Standardschreibens. Je Vorlage gibt es höchstens eine veröffentlichte Version;
  neue Schreiben entstehen mit ihr, gespeicherte behalten ihre Versionen
  (ADR-007, ADR-022). Vorher entsteht ein Probe-PDF mit den Beispieldaten der
  Version und wird wie jedes gespeicherte Schreiben geprüft (ADR-005). Besteht es
  die Prüfung nicht, bleibt der Entwurf ein Entwurf.

  Grundlage:
    Angenommen das Briefpapier "Standard" hat die veröffentlichte Version 1 mit den Beispieldaten "absender" = "Muster GmbH" und "betreff" = "Allgemein"
    Und das Standardschreiben "Mahnung" nutzt dieses Briefpapier
    Und seine Version 1 ist veröffentlicht, und sein Entwurf 2 hat die Beispieldaten "name" = "Erika Mustermann GmbH" und "betreff" = "Mahnung"
    Und Vera darf Vorlagen veröffentlichen

  Szenario: Ein konformer Entwurf wird veröffentlicht und löst die bisherige Version ab
    Wenn Vera die Mahnung veröffentlicht
    Dann ist Version 2 der Mahnung veröffentlicht
    Und Version 1 ist abgelöst und unverändert

  Szenario: Das Probe-PDF entsteht mit dem veröffentlichten Briefpapier und den Beispieldaten beider Versionen
    Wenn Vera die Mahnung veröffentlicht
    Dann entsteht das Probe-PDF aus Briefpapier Version 1 und Mahnung Version 2
    Und mit den Beispieldaten "absender" = "Muster GmbH", "betreff" = "Mahnung" und "name" = "Erika Mustermann GmbH"
    Und genau dieses Probe-PDF wurde geprüft

  Szenario: Ein Briefpapier wird mit dem eingebauten Beispielinhalt geprüft und veröffentlicht
    Angenommen das Briefpapier hat einen Entwurf 2
    Wenn Vera das Briefpapier veröffentlicht
    Dann entsteht das Probe-PDF aus dem Entwurf und dem eingebauten Beispielinhalt
    Und mit den Beispieldaten des Briefpapiers
    Und Version 2 des Briefpapiers ist veröffentlicht, Version 1 abgelöst

  Szenario: Besteht das Probe-PDF die Prüfung nicht, bleibt die Version ein Entwurf
    Angenommen die Prüfung findet "Bild ohne Alternativtext"
    Wenn Vera die Mahnung veröffentlicht
    Dann ist die Mahnung nicht konform mit dem Befund "Bild ohne Alternativtext"
    Und die Mahnung ist unverändert

  Szenario: Lässt sich die Prüfung nicht ausführen, wird nicht veröffentlicht
    Angenommen die Prüfung ist nicht ausführbar
    Wenn Vera die Mahnung veröffentlicht
    Dann scheitert das Veröffentlichen mit einem technischen Fehler
    Und die Mahnung ist unverändert

  Szenario: Ohne Berechtigung wird nichts geprüft und nichts veröffentlicht
    Wenn Max die Mahnung veröffentlicht
    Dann ist das Veröffentlichen nicht erlaubt
    Und es entstand kein Probe-PDF
    Und die Mahnung ist unverändert

  Szenario: Ohne Entwurf gibt es nichts zu veröffentlichen
    Angenommen die Mahnung hat keinen Entwurf
    Wenn Vera die Mahnung veröffentlicht
    Dann hat die Mahnung keinen Entwurf

  Szenario: Ein Entwurf ohne Inhalt wird nicht veröffentlicht
    Angenommen der Entwurf der Mahnung enthält nur Leerraum
    Wenn Vera die Mahnung veröffentlicht
    Dann hat der Entwurf keinen Inhalt
    Und es entstand kein Probe-PDF

  Szenario: Ohne veröffentlichtes Briefpapier wird das Standardschreiben nicht veröffentlicht
    Angenommen das Briefpapier ist zurückgezogen
    Wenn Vera die Mahnung veröffentlicht
    Dann ist das Briefpapier nicht veröffentlicht
    Und es entstand kein Probe-PDF

  Szenario: Eine unbekannte Vorlage wird gemeldet
    Wenn Vera eine Vorlage mit unbekannter ID veröffentlicht
    Dann ist die Vorlage unbekannt

  Szenario: Hat ein anderer die Vorlage inzwischen geändert, wird nichts veröffentlicht
    Angenommen ein anderer Vorlagenverwalter ändert den Entwurf, während Vera veröffentlicht
    Wenn Vera die Mahnung veröffentlicht
    Dann ist der Stand veraltet
    Und der geänderte, ungeprüfte Entwurf bleibt ein Entwurf

  Szenario: Eine veröffentlichte Version bleibt unverändert, eine Änderung beginnt einen neuen Entwurf
    Angenommen Vera hat die Mahnung veröffentlicht
    Wenn die veröffentlichte Version geändert werden soll
    Dann wird das abgelehnt
    Und ein neuer Entwurf 3 übernimmt die Beispieldaten von Version 2
