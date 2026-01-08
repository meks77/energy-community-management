# language: de
Funktionalität: Rechnungs-Import

  Szenario: Import neuer Rechnungen
    Angenommen folgende Rechnungen sind in der Import-Quelle:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1001            | R-2023-001      | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |
      | 1002            | R-2023-002      | 2023-12-05 | 50.00        | PRIVAT                 |
    Wenn der Rechnungs-Import ausgeführt wird
    Dann sind folgende Rechnungen im System vorhanden:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1001            | R-2023-001      | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |
      | 1002            | R-2023-002      | 2023-12-05 | 50.00        | PRIVAT                 |

  Szenario: Import bereits importierter Rechnungen mit identen Werten
    Angenommen folgende Rechnungen wurden bereits importiert:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1001            | R-2023-001      | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |
    Und folgende Rechnungen sind in der Import-Quelle:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1001            | R-2023-001      | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |
    Wenn der Rechnungs-Import ausgeführt wird
    Dann sind folgende Rechnungen im System vorhanden:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1001            | R-2023-001      | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |

  Szenario: Import bereits importierter Rechnungen mit geänderten Werten führt zu Fehler
    Angenommen folgende Rechnungen wurden bereits importiert:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1001            | R-2023-001      | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |
    Und folgende Rechnungen sind in der Import-Quelle:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1002            | R-2023-001      | 2023-12-02 | 130.00       | PRIVAT                 |
      | 1003            | R-2023-003      | 2023-12-10 | 200.00       | UMSATZSTEUERPFLICHTIG  |
    Wenn der Rechnungs-Import ausgeführt wird
    Dann gab es einen Fehler beim Rechnungs-Import
    Und sind folgende Rechnungen im System vorhanden:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1001            | R-2023-001      | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |

  Szenario: Import einer Rechnung mit neuer Rechnungsnummer führt zu neuer Rechnung
    Angenommen folgende Rechnungen wurden bereits importiert:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1001            | R-2023-001      | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |
    Und folgende Rechnungen sind in der Import-Quelle:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1001            | R-2023-001-NEU  | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |
    Wenn der Rechnungs-Import ausgeführt wird
    Dann sind folgende Rechnungen im System vorhanden:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1001            | R-2023-001      | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |
      | 1001            | R-2023-001-NEU  | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |
