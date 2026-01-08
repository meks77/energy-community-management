package at.hillstrom.energy.usecases.unbezahlterechnungen

import at.hillstrom.energy.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class ZahlungsMatcherTest {
    
    private val generator = object : EventSequenceGenerator {
        private var lastSequence = 0L
        override fun nextSequence(): Long = ++lastSequence
    }
    private val matcher = ZahlungsMatcher(generator)

    @Test
    fun `erkennt Rechnungsnummer aus Zahlungsreferenz`() {
        val event = erstelleZahlungEvent(zahlungsreferenz = "2025001")
        
        val result = matcher.handle(event)

        assertTrue(result is RechnungBeglichen)
        assertEquals(Rechnungsnummer("RN_2025001"), (result as RechnungBeglichen).rechnungsnummer)
    }

    @Test
    fun `erkennt Rechnungsnummer aus Buchungsdetails`() {
        val event = erstelleZahlungEvent(buchungsdetails = "Begleichung von RN_2025002")
        
        val result = matcher.handle(event)
        
        assertTrue(result is RechnungBeglichen)
        assertEquals(Rechnungsnummer("RN_2025002"), (result as RechnungBeglichen).rechnungsnummer)
    }

    @Test
    fun `gibt ZahlungNichtZugeordnet zurueck wenn keine Rechnungsnummer gefunden wird`() {
        val event = erstelleZahlungEvent(buchungsdetails = "Miete März")
        
        val result = matcher.handle(event)
        
        assertTrue(result is ZahlungNichtZugeordnet)
        assertEquals(event.buchungsreferenz, (result as ZahlungNichtZugeordnet).buchungsreferenz)
    }

    private fun erstelleZahlungEvent(
        zahlungsreferenz: String? = null,
        buchungsdetails: String? = null
    ) = KontoumsatzImportiert(
        buchungsreferenz = Buchungsreferenz("REF123"),
        properties = KontoumsatzProperties(
            buchungsdatum = Buchungsdatum(Datum(LocalDate.now())),
            partnername = Partnername("Max Mustermann"),
            partnerIban = IBAN("AT123"),
            betrag = Kontoumsatzbetrag(Betrag(BigDecimal("120"))),
            buchungsdetails = buchungsdetails?.let { Buchungsdetails(it) },
            buchungsreferenz = Buchungsreferenz("REF123"),
            zahlungsreferenz = zahlungsreferenz?.let { Zahlungsreferenz(it) },
            mandatsId = null
        ),
        sequenznummer = generator.nextSequence()
    )
}
