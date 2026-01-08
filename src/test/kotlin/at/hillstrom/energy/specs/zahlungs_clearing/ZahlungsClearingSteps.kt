package at.hillstrom.energy.specs.zahlungs_clearing

import at.hillstrom.energy.*
import at.hillstrom.energy.specs.SharedAppContext
import at.hillstrom.energy.usecases.unbezahlterechnungen.UnbezahlteRechnung
import io.cucumber.java.de.Angenommen
import io.cucumber.java.de.Dann
import io.cucumber.java.de.Wenn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class ZahlungsClearingSteps(private val context: SharedAppContext) {

    private val app get() = context.app
    private var abfrageErgebnis: List<UnbezahlteRechnung> = emptyList()
    private val geworfeneEvents = mutableListOf<ZahlungsClearingEvent>()

    @Angenommen("eine Rechnung {string} über {double} Euro wurde am {string} erstellt")
    @Wenn("eine Rechnung {string} über {double} Euro am {string} erstellt wird")
    fun eine_rechnung_ueber_euro_wurde_am_erstellt(nr: String, betrag: Double, datum: String) {
        val event = RechnungErstellt(
            id = Rechnungsnummer(nr),
            properties = RechnungProperties(
                mitgliedsnummer = Mitgliedsnummer("M1"),
                nettobetrag = Nettobetrag(Betrag(BigDecimal.valueOf(betrag))), // Vereinfacht
                bruttobetrag = Bruttobetrag(Betrag(BigDecimal.valueOf(betrag))),
                umsatzsteuer = Umsatzsteuer(Betrag(BigDecimal.ZERO)),
                rechnungsnummer = Rechnungsnummer(nr),
                rechnungsdatum = Rechnungsdatum(Datum(LocalDate.parse(datum))),
                faelligkeitsdatum = Faelligkeitsdatum(Datum(LocalDate.parse(datum).plusDays(14))),
                steuerklasse = RechnungsSteuerklasse.PRIVAT
            )
        )
        app.zahlungsClearingReadModel.handle(event)
    }

    @Wenn("ich die Liste der unbezahlten Rechnungen ab {string} abfrage")
    fun ich_die_liste_der_unbezahlten_rechnungen_ab_abfrage(datum: String) {
        abfrageErgebnis = app.zahlungsClearingReadModel.getUnbezahlteRechnungen(Rechnungsdatum(Datum(LocalDate.parse(datum))))
    }

    @Dann("enthält die Liste die Rechnung {string}")
    fun enthaelt_die_liste_die_rechnung(nr: String) {
        assertTrue(abfrageErgebnis.any { it.rechnungsnummer.wert == nr })
    }

    @Wenn("ein Kontoumsatz mit dem Verwendungszweck {string} über {double} Euro importiert wird")
    @Angenommen("ein Kontoumsatz mit dem Verwendungszweck {string} über {double} Euro wird importiert")
    fun ein_kontoumsatz_mit_dem_verwendungszweck_ueber_euro_importiert_wird(verwendungszweck: String, betrag: Double) {
        val event = KontoumsatzImportiert(
            buchungsreferenz = Buchungsreferenz(UUID.randomUUID().toString()),
            properties = KontoumsatzProperties(
                buchungsdatum = Buchungsdatum(Datum(LocalDate.now())),
                partnername = Partnername("Max Mustermann"),
                partnerIban = IBAN("AT123"),
                betrag = Kontoumsatzbetrag(Betrag(BigDecimal.valueOf(betrag))),
                buchungsdetails = Buchungsdetails(verwendungszweck),
                buchungsreferenz = Buchungsreferenz("REF"),
                zahlungsreferenz = null,
                mandatsId = null
            )
        )
        handleKontoumsatz(event)
    }

    @Wenn("ein Kontoumsatz mit der Zahlungsreferenz {string} über {double} Euro importiert wird")
    @Angenommen("ein Kontoumsatz mit der Zahlungsreferenz {string} über {double} Euro wird importiert")
    fun ein_kontoumsatz_mit_der_zahlungsreferenz_ueber_euro_importiert_wird(zahlungsreferenzWert: String, betrag: Double) {
        val event = KontoumsatzImportiert(
            buchungsreferenz = Buchungsreferenz(UUID.randomUUID().toString()),
            properties = KontoumsatzProperties(
                buchungsdatum = Buchungsdatum(Datum(LocalDate.now())),
                partnername = Partnername("Max Mustermann"),
                partnerIban = IBAN("AT123"),
                betrag = Kontoumsatzbetrag(Betrag(BigDecimal.valueOf(betrag))),
                buchungsdetails = null,
                buchungsreferenz = Buchungsreferenz("REF"),
                zahlungsreferenz = Zahlungsreferenz(zahlungsreferenzWert),
                mandatsId = null
            )
        )
        handleKontoumsatz(event)
    }

    private fun handleKontoumsatz(event: KontoumsatzImportiert) {
        val resultEvent = app.zahlungsMatcher.handle(event)
        geworfeneEvents.add(resultEvent)
        if (resultEvent is RechnungBeglichen) {
            app.zahlungsClearingReadModel.handle(resultEvent)
        }
    }

    @Dann("wurde das Ereignis geworfen, dass die Zahlung nicht zugeordnet werden konnte")
    fun wurde_das_ereignis_geworfen_dass_die_zahlung_nicht_zugeordnet_werden_konnte() {
        assertTrue(geworfeneEvents.any { it is ZahlungNichtZugeordnet })
    }

    @Dann("ist die Liste der unbezahlten Rechnungen leer")
    fun ist_die_liste_der_unbezahlten_rechnungen_leer() {
        assertTrue(abfrageErgebnis.isEmpty())
    }

    @Dann("enthält die Liste nur die Rechnung {string}")
    fun enthaelt_die_liste_nur_die_rechnung(nr: String) {
        assertEquals(1, abfrageErgebnis.size)
        assertEquals(nr, abfrageErgebnis[0].rechnungsnummer.wert)
    }
}
