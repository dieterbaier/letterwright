# language: de
# Status: proposed (KI-gestützt, wartet auf Review). Brücke zu den Tests:
# EingebauteBerechtigungspruefungTest im Modul spring-boot-starter, ein Test je Szenario,
# benannt nach dem Szenariotitel.
Funktionalität: Eingebaute Berechtigungsprüfung
  Stellt die Host-Anwendung keine eigene Berechtigungsprüfung bereit, entscheidet
  die Bibliothek über die Rollen aus der Spring Security des Hosts. Jede Aktion
  braucht ihre eigene Rolle, standardmäßig ROLE_SSW_<AKTION>.

  Grundlage:
    Angenommen die Host-Anwendung stellt keine eigene Berechtigungsprüfung bereit

  Szenario: Mit der Rolle der Aktion ist sie erlaubt
    Angenommen Erika ist angemeldet und hat die Rolle "ROLE_SSW_SCHREIBEN_ERZEUGEN"
    Wenn die Bibliothek fragt, ob Erika zum Kunden 4711 ein Schreiben erzeugen darf
    Dann ist es erlaubt

  Szenario: Mit der Rolle einer anderen Aktion ist sie nicht erlaubt
    Angenommen Erika ist angemeldet und hat nur die Rolle "ROLE_SSW_SCHREIBEN_ANSEHEN"
    Wenn die Bibliothek fragt, ob Erika zum Kunden 4711 ein Schreiben erzeugen darf
    Dann ist es nicht erlaubt

  Szenario: Ohne Anmeldung ist nichts erlaubt
    Angenommen niemand ist angemeldet
    Wenn die Bibliothek fragt, ob Erika zum Kunden 4711 ein Schreiben erzeugen darf
    Dann ist es nicht erlaubt

  Szenario: Für einen anderen als den angemeldeten Benutzer ist nichts erlaubt
    Angenommen Max ist angemeldet und hat die Rolle "ROLE_SSW_SCHREIBEN_ERZEUGEN"
    Wenn die Bibliothek fragt, ob Erika zum Kunden 4711 ein Schreiben erzeugen darf
    Dann ist es nicht erlaubt

  Szenario: Eine konfigurierte Rolle ersetzt die vorgegebene
    Angenommen der Host hat für das Erzeugen von Schreiben die Rolle "ROLE_SACHBEARBEITUNG" konfiguriert
    Und Erika ist angemeldet und hat nur die Rolle "ROLE_SSW_SCHREIBEN_ERZEUGEN"
    Wenn die Bibliothek fragt, ob Erika zum Kunden 4711 ein Schreiben erzeugen darf
    Dann ist es nicht erlaubt

  Szenario: Eine eigene Berechtigungsprüfung des Hosts ersetzt die eingebaute
    Angenommen die Host-Anwendung stellt doch eine eigene Berechtigungsprüfung bereit, die alles erlaubt
    Und niemand ist angemeldet
    Wenn die Bibliothek fragt, ob Erika zum Kunden 4711 ein Schreiben erzeugen darf
    Dann ist es erlaubt
