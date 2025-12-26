# language: de
Funktionalität: Rechnungs-Import

  Szenario: Import neuer Rechnungen
    Gegeben seien folgende Rechnungen in der Import-Quelle:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag | Steuerklasse           |
      | 1001            | R-2023-001      | 2023-12-01 | 120.00       | UMSATZSTEUERPFLICHTIG  |
      | 1002            | R-2023-002      | 2023-12-05 | 50.00        | PRIVAT                 |
    Wenn der Rechnungs-Import ausgeführt wird
    Dann sind folgende Rechnungen im System vorhanden:
      | Mitgliedsnummer | Rechnungsnummer | Datum      | Bruttobetrag |
      | 1001            | R-2023-001      | 2023-12-01 | 120.00       |
      | 1002            | R-2023-002      | 2023-12-05 | 50.00        |
