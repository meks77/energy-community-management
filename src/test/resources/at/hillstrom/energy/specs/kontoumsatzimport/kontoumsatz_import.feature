# language: de
Funktionalität: Kontoumsatz-Import

  Szenario: Import neuer Kontoumsätze
    Gegeben seien folgende Kontoumsätze in der Import-Quelle:
      | Datum      | Partner           | Betrag | Referenz    |
      | 2023-12-10 | Max Mustermann    | 120.00 | REF-001     |
      | 2023-12-11 | Erika Musterfrau  | 50.00  | REF-002     |
    Wenn der Kontoumsatz-Import ausgeführt wird
    Dann sind folgende Kontoumsätze im System vorhanden:
      | Datum      | Partner           | Betrag | Referenz    |
      | 2023-12-10 | Max Mustermann    | 120.00 | REF-001     |
      | 2023-12-11 | Erika Musterfrau  | 50.00  | REF-002     |
