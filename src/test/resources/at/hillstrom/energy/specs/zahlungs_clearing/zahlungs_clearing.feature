# language: de
Funktionalität: Automatisches Zahlungs-Clearing
  Das Read-Model soll Buch führen über offene und bezahlte Rechnungen basierend auf Events.

  Szenario: Eine neue Rechnung wird als offen erfasst
    Angenommen eine Rechnung "RN_100" über 120.00 Euro wurde am "2023-01-01" erstellt
    Wenn ich die Liste der unbezahlten Rechnungen ab "2023-01-01" abfrage
    Dann enthält die Liste die Rechnung "RN_100"

  Szenario: Eine Zahlung mit Verwendungszweck nach Rechnungserstellung markiert diese als bezahlt
    Angenommen eine Rechnung "RN_101" über 150.00 Euro wurde am "2023-01-01" erstellt
    Wenn ein Kontoumsatz mit dem Verwendungszweck "Zahlung RN_101" über 150.00 Euro importiert wird
    Und ich die Liste der unbezahlten Rechnungen ab "2023-01-01" abfrage
    Dann ist die Liste der unbezahlten Rechnungen leer

  Szenario: Eine Zahlung mit Zahlungsreferenz Rechnungserstellung markiert diese als bezahlt
    Angenommen eine Rechnung "RN_101" über 150.00 Euro wurde am "2023-01-01" erstellt
    Wenn ein Kontoumsatz mit der Zahlungsreferenz "101" über 150.00 Euro importiert wird
    Und ich die Liste der unbezahlten Rechnungen ab "2023-01-01" abfrage
    Dann ist die Liste der unbezahlten Rechnungen leer

  Szenario: Eine Zahlung vor der Rechnungserstellung wird korrekt zugeordnet
    Angenommen ein Kontoumsatz mit dem Verwendungszweck "RN_102 Begleichung" über 200.00 Euro wird importiert
    Wenn eine Rechnung "RN_102" über 200.00 Euro am "2023-01-05" erstellt wird
    Und ich die Liste der unbezahlten Rechnungen ab "2023-01-01" abfrage
    Dann ist die Liste der unbezahlten Rechnungen leer

  Szenario: Rechnungen vor dem Stichtag werden nicht angezeigt
    Angenommen eine Rechnung "RN_103" über 100.00 Euro wurde am "2023-01-01" erstellt
    Und eine Rechnung "RN_104" über 100.00 Euro wurde am "2023-02-01" erstellt
    Wenn ich die Liste der unbezahlten Rechnungen ab "2023-01-15" abfrage
    Dann enthält die Liste nur die Rechnung "RN_104"

  Szenario: Eine Zahlung ohne Rechnungsbezug löst ein Event aus
    Wenn ein Kontoumsatz mit dem Verwendungszweck "Unbekannte Zahlung" über 50.00 Euro importiert wird
    Dann wurde das Ereignis geworfen, dass die Zahlung nicht zugeordnet werden konnte
