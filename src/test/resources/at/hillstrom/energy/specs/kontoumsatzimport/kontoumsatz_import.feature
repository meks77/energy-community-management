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

  Szenario: Import bereits importierter Kontoumsätze mit identen Werten
    Angenommen folgende Kontoumsätze wurden bereits importiert:
      | Datum      | Partner           | Betrag | Referenz    |
      | 2023-12-10 | Max Mustermann    | 120.00 | REF-001     |
    Und folgende Kontoumsätze in der Import-Quelle:
      | Datum      | Partner           | Betrag | Referenz    |
      | 2023-12-10 | Max Mustermann    | 120.00 | REF-001     |
    Wenn der Kontoumsatz-Import ausgeführt wird
    Dann sind folgende Kontoumsätze im System vorhanden:
      | Datum      | Partner           | Betrag | Referenz    |
      | 2023-12-10 | Max Mustermann    | 120.00 | REF-001     |

  Szenario: Import bereits importierter Kontoumsätze mit geänderten Werten führt zu Fehler
    Angenommen folgende Kontoumsätze wurden bereits importiert:
      | Datum      | Partner           | Betrag | Referenz    |
      | 2023-12-10 | Max Mustermann    | 120.00 | REF-001     |
    Und folgende Kontoumsätze in der Import-Quelle:
      | Datum      | Partner           | Betrag | Referenz    |
      | 2023-12-10 | Max Mustermann    | 130.00 | REF-001     |
      | 2023-12-11 | Erika Musterfrau  | 50.00  | REF-002     |
    Wenn der Kontoumsatz-Import ausgeführt wird
    Dann gab es einen Fehler beim Kontoumsatz-Import
    Und sind folgende Kontoumsätze im System vorhanden:
      | Datum      | Partner           | Betrag | Referenz    |
      | 2023-12-10 | Max Mustermann    | 120.00 | REF-001     |

  Szenario: Import eines Kontoumsatzes mit neuer Referenz führt zu neuem Kontoumsatz
    Angenommen folgende Kontoumsätze wurden bereits importiert:
      | Datum      | Partner           | Betrag | Referenz    |
      | 2023-12-10 | Max Mustermann    | 120.00 | REF-001     |
    Und folgende Kontoumsätze in der Import-Quelle:
      | Datum      | Partner           | Betrag | Referenz    |
      | 2023-12-11 | Erika Musterfrau  | 50.00  | REF-002     |
    Wenn der Kontoumsatz-Import ausgeführt wird
    Dann sind folgende Kontoumsätze im System vorhanden:
      | Datum      | Partner           | Betrag | Referenz    |
      | 2023-12-10 | Max Mustermann    | 120.00 | REF-001     |
      | 2023-12-11 | Erika Musterfrau  | 50.00  | REF-002     |
