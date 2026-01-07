package at.hillstrom.energy.usecases.unbezahlterechnungen

import at.hillstrom.energy.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class ZahlungsClearingReadModelTest {

    private val repository = InMemoryUnbezahlteRechnungenRepository()
    private val readModel = ZahlungsClearingReadModel(repository)

    @Test
    fun `Rechnung wird als offen erfasst`() {
        val rechnungsnummer = Rechnungsnummer("RN_20251200000331")
        val event = erstelleRechnungEvent(rechnungsnummer)

        readModel.handle(event)

        val unbezahlte = readModel.getUnbezahlteRechnungen(Rechnungsdatum(Datum(LocalDate.MIN)))
        assertEquals(1, unbezahlte.size)
        assertEquals(rechnungsnummer, unbezahlte[0].rechnungsnummer)
        assertEquals(RechnungsStatus.OFFEN, unbezahlte[0].status)
    }

    @Test
    fun `Zahlung nach Rechnung markiert Rechnung als bezahlt`() {
        val rechnungsnummer = Rechnungsnummer("RN_20251200000331")
        readModel.handle(erstelleRechnungEvent(rechnungsnummer))
        
        readModel.handle(erstelleRechnungBeglichenEvent(rechnungsnummer))

        val unbezahlte = readModel.getUnbezahlteRechnungen(Rechnungsdatum(Datum(LocalDate.MIN)))
        assertTrue(unbezahlte.isEmpty())
    }

    @Test
    fun `Zahlung vor Rechnung markiert Rechnung sofort als bezahlt`() {
        val rechnungsnummer = Rechnungsnummer("RN_20251200000331")
        
        readModel.handle(erstelleRechnungBeglichenEvent(rechnungsnummer))
        readModel.handle(erstelleRechnungEvent(rechnungsnummer))

        val unbezahlte = readModel.getUnbezahlteRechnungen(Rechnungsdatum(Datum(LocalDate.MIN)))
        assertTrue(unbezahlte.isEmpty())
    }

    @Test
    fun `Query filtert nach Datum`() {
        val r1 = Rechnungsnummer("RN_20251200000331")
        val r2 = Rechnungsnummer("RN_20251200000330")
        
        readModel.handle(erstelleRechnungEvent(r1, LocalDate.of(2023, 1, 1)))
        readModel.handle(erstelleRechnungEvent(r2, LocalDate.of(2023, 2, 1)))

        val unbezahlte = readModel.getUnbezahlteRechnungen(Rechnungsdatum(Datum(LocalDate.of(2023, 1, 15))))
        assertEquals(1, unbezahlte.size)
        assertEquals(r2, unbezahlte[0].rechnungsnummer)
    }

    private fun erstelleRechnungEvent(
        rechnungsnummer: Rechnungsnummer, 
        datum: LocalDate = LocalDate.now()
    ) = RechnungErstellt(
        id = UUID.randomUUID(),
        properties = RechnungProperties(
            mitgliedsnummer = Mitgliedsnummer("M1"),
            nettobetrag = Nettobetrag(Betrag(BigDecimal("100"))),
            bruttobetrag = Bruttobetrag(Betrag(BigDecimal("120"))),
            umsatzsteuer = Umsatzsteuer(Betrag(BigDecimal("20"))),
            rechnungsnummer = rechnungsnummer,
            rechnungsdatum = Rechnungsdatum(Datum(datum)),
            faelligkeitsdatum = Faelligkeitsdatum(Datum(datum.plusDays(14))),
            steuerklasse = RechnungsSteuerklasse.PRIVAT
        )
    )

    private fun erstelleRechnungBeglichenEvent(rechnungsnummer: Rechnungsnummer) = RechnungBeglichen(
        rechnungsnummer = rechnungsnummer,
        buchungsreferenz = Buchungsreferenz("REF123"),
        beglichenAm = Datum(LocalDate.now())
    )

    class InMemoryUnbezahlteRechnungenRepository : UnbezahlteRechnungenRepository {
        private val rechnungen = mutableMapOf<Rechnungsnummer, UnbezahlteRechnung>()
        private val vorabBezahlt = mutableSetOf<Rechnungsnummer>()

        override fun save(rechnung: UnbezahlteRechnung) {
            rechnungen[rechnung.rechnungsnummer] = rechnung
        }

        override fun findByRechnungsnummer(rechnungsnummer: Rechnungsnummer): UnbezahlteRechnung? {
            return rechnungen[rechnungsnummer]
        }

        override fun findUnbezahlteAb(datum: Rechnungsdatum): List<UnbezahlteRechnung> {
            return rechnungen.values.filter { it.rechnungsdatum.datum.wert >= datum.datum.wert }
        }

        override fun markiereAlsBezahlt(rechnungsnummer: Rechnungsnummer) {
            vorabBezahlt.add(rechnungsnummer)
        }

        override fun istAlsBezahltMarkiert(rechnungsnummer: Rechnungsnummer): Boolean {
            return vorabBezahlt.contains(rechnungsnummer)
        }
    }
}
