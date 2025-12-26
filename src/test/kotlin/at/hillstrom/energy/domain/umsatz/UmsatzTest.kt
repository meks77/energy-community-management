package at.hillstrom.energy.domain.umsatz
import at.hillstrom.energy.*
import at.hillstrom.energy.usecases.kontoumsatzimport.Kontoumsatz
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class UmsatzTest {

    @Test
    fun `importiert einen Umsatz`() {
        val properties = KontoumsatzProperties(
            buchungsdatum = Buchungsdatum(Datum(LocalDate.of(2023, 12, 24))),
            partnername = Partnername("Max Mustermann"),
            partnerIban = IBAN("AT123456789012345678"),
            betrag = Kontoumsatzbetrag(Betrag(BigDecimal("100.50"))),
            buchungsdetails = Buchungsdetails("Geschenk"),
            buchungsreferenz = Buchungsreferenz("REF-123"),
            zahlungsreferenz = Zahlungsreferenz("Z-REF-123"),
            mandatsId = MandatsId("MANDAT-1")
        )

        val event = Kontoumsatz.importiereUmsatz(properties)
        val kontoumsatz = Kontoumsatz(event)

        assertEquals(properties, kontoumsatz.properties)
        assertEquals(Buchungsreferenz("REF-123"), kontoumsatz.buchungsreferenz)
    }

    @Test
    fun `validiert Gleichheit bei Duplikaten`() {
        val properties = KontoumsatzProperties(
            buchungsdatum = Buchungsdatum(Datum(LocalDate.of(2023, 12, 24))),
            partnername = Partnername("Max Mustermann"),
            partnerIban = IBAN("AT123456789012345678"),
            betrag = Kontoumsatzbetrag(Betrag(BigDecimal("100.50"))),
            buchungsdetails = Buchungsdetails("Geschenk"),
            buchungsreferenz = Buchungsreferenz("REF-123"),
            zahlungsreferenz = Zahlungsreferenz("Z-REF-123"),
            mandatsId = MandatsId("MANDAT-1")
        )

        val kontoumsatz = Kontoumsatz(Kontoumsatz.importiereUmsatz(properties))

        // Gleiche Properties sollten kein Problem sein
        assertDoesNotThrow {
            kontoumsatz.validiereGleichheit(properties)
        }

        // Abweichende Properties sollten Fehler werfen
        val abweichendeProperties = properties.copy(betrag = Kontoumsatzbetrag(Betrag(BigDecimal("200.00"))))
        val exception = assertThrows(IllegalArgumentException::class.java) {
            kontoumsatz.validiereGleichheit(abweichendeProperties)
        }
        assertTrue(exception.message!!.contains("REF-123"))
    }
}
