package at.hillstrom.energy.domain.umsatz

import at.hillstrom.energy.domain.rechnung.Betrag
import at.hillstrom.energy.domain.rechnung.Datum
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class UmsatzTest {

    @Test
    fun `importiert einen Umsatz`() {
        val properties = UmsatzProperties(
            buchungsdatum = Buchungsdatum(Datum(LocalDate.of(2023, 12, 24))),
            partnername = Partnername("Max Mustermann"),
            partnerIban = IBAN("AT123456789012345678"),
            betrag = Umsatzbetrag(Betrag(BigDecimal("100.50"))),
            buchungsdetails = Buchungsdetails("Geschenk"),
            buchungsreferenz = Buchungsreferenz("REF-123"),
            zahlungsreferenz = Zahlungsreferenz("Z-REF-123"),
            mandatsId = MandatsId("MANDAT-1")
        )

        val event = Umsatz.importiereUmsatz(properties)
        val umsatz = Umsatz(event)

        assertEquals(properties, umsatz.properties)
        assertEquals(Buchungsreferenz("REF-123"), umsatz.buchungsreferenz)
    }

    @Test
    fun `validiert Gleichheit bei Duplikaten`() {
        val properties = UmsatzProperties(
            buchungsdatum = Buchungsdatum(Datum(LocalDate.of(2023, 12, 24))),
            partnername = Partnername("Max Mustermann"),
            partnerIban = IBAN("AT123456789012345678"),
            betrag = Umsatzbetrag(Betrag(BigDecimal("100.50"))),
            buchungsdetails = Buchungsdetails("Geschenk"),
            buchungsreferenz = Buchungsreferenz("REF-123"),
            zahlungsreferenz = Zahlungsreferenz("Z-REF-123"),
            mandatsId = MandatsId("MANDAT-1")
        )

        val umsatz = Umsatz(Umsatz.importiereUmsatz(properties))

        // Gleiche Properties sollten kein Problem sein
        assertDoesNotThrow {
            umsatz.validiereGleichheit(properties)
        }

        // Abweichende Properties sollten Fehler werfen
        val abweichendeProperties = properties.copy(betrag = Umsatzbetrag(Betrag(BigDecimal("200.00"))))
        val exception = assertThrows(IllegalArgumentException::class.java) {
            umsatz.validiereGleichheit(abweichendeProperties)
        }
        assertTrue(exception.message!!.contains("REF-123"))
    }
}
