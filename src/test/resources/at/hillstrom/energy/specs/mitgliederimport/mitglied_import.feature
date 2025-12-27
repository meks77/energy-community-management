# language: de
Funktionalität: Mitglieder Import

  Szenario: Import neuer Mitglieder
    Angenommen folgende Mitglieder sind in der Import-Quelle:
      | Kundennummer | Name             | E-Mail            |
      | 1001         | Max Mustermann   | max@example.com   |
      | 1002         | Erika Musterfrau | erika@example.com |
    Wenn der Import ausgeführt wird
    Dann sind folgende Mitglieder im System vorhanden:
      | Kundennummer | Name             | E-Mail            |
      | 1001         | Max Mustermann   | max@example.com   |
      | 1002         | Erika Musterfrau | erika@example.com |

  Szenario: Import bereits vorhandener Mitglieder
    Angenommen folgende Mitglieder wurden bereits importiert:
      | Kundennummer | Name             | E-Mail            |
      | 1001         | Max Mustermann   | max@example.com   |
    Und folgende Mitglieder sind in der Import-Quelle:
      | Kundennummer | Name             | E-Mail            |
      | 1001         | Max Mustermann   | max@example.com   |
    Wenn der Import ausgeführt wird
    Dann sind folgende Mitglieder im System vorhanden:
      | Kundennummer | Name             | E-Mail            |
      | 1001         | Max Mustermann   | max@example.com   |

  Szenario: Import geänderter Daten bei vorhandenen Mitgliedern
    Angenommen folgende Mitglieder wurden bereits importiert:
      | Kundennummer | Name             | E-Mail            |
      | 1001         | Max Mustermann   | max@example.com   |
    Und folgende Mitglieder sind in der Import-Quelle:
      | Kundennummer | Name             | E-Mail            |
      | 1001         | Max Neu-Name     | max_neu@example.com |
    Wenn der Import ausgeführt wird
    Dann sind folgende Mitglieder im System vorhanden:
      | Kundennummer | Name             | E-Mail            |
      | 1001         | Max Neu-Name     | max_neu@example.com |
