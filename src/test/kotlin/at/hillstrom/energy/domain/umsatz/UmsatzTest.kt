package at.hillstrom.energy.domain.umsatz

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class UmsatzTest {

    @Test
    fun `importiert einen Umsatz`() {
        val properties = UmsatzProperties(
            buchungsdatum = LocalDate.of(2023, 12, 24),
            partnername = "Max Mustermann",
            partnerIban = "AT123456789012345678",
            betrag = BigDecimal("100.50"),
            buchungsdetails = "Geschenk",
            buchungsreferenz = "REF-123",
            zahlungsreferenz = "Z-REF-123",
            mandatsId = "MANDAT-1"
        )

        val event = Umsatz.importiereUmsatz(properties)
        val umsatz = Umsatz(event)

        assertEquals(properties, umsatz.properties)
        assertEquals("REF-123", umsatz.buchungsreferenz)
    }

    @Test
    fun `validiert Gleichheit bei Duplikaten`() {
        val properties = UmsatzProperties(
            buchungsdatum = LocalDate.of(2023, 12, 24),
            partnername = "Max Mustermann",
            partnerIban = "AT123456789012345678",
            betrag = BigDecimal("100.50"),
            buchungsdetails = "Geschenk",
            buchungsreferenz = "REF-123",
            zahlungsreferenz = "Z-REF-123",
            mandatsId = "MANDAT-1"
        )

        val umsatz = Umsatz(Umsatz.importiereUmsatz(properties))

        // Gleiche Properties sollten kein Problem sein
        assertDoesNotThrow {
            umsatz.validiereGleichheit(properties)
        }

        // Abweichende Properties sollten Fehler werfen
        val abweichendeProperties = properties.copy(betrag = BigDecimal("200.00"))
        val exception = assertThrows(IllegalArgumentException::class.java) {
            umsatz.validiereGleichheit(abweichendeProperties)
        }
        assertTrue(exception.message!!.contains("REF-123"))
    }
}
