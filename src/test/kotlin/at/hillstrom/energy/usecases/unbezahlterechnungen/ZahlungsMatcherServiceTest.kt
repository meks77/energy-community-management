package at.hillstrom.energy.usecases.unbezahlterechnungen

import at.hillstrom.energy.*
import at.hillstrom.energy.usecases.importe.kontoumsatzimport.KontoumsatzRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class ZahlungsMatcherServiceTest {

    private class MockKontoumsatzRepository : KontoumsatzRepository {
        val events = mutableListOf<KontoumsatzEvent>()
        override fun speichereEvents(events: List<KontoumsatzEvent>) {
            this.events.addAll(events)
        }
        override fun ladeEvents(buchungsreferenz: Buchungsreferenz): List<KontoumsatzEvent> = events.filter { it.buchungsreferenz == buchungsreferenz }
        override fun alleLaden(): List<KontoumsatzEvent> = events
        override fun ladeAbSequenz(sequenznummer: Long): List<KontoumsatzEvent> =
            events.filter { it.sequenznummer > sequenznummer }.sortedBy { it.sequenznummer }
    }

    private class MockProcessedEventRepository : ProcessedEventRepository {
        var lastSequence = 0L
        override fun saveLastProcessedSequence(sequence: Long) { lastSequence = sequence }
        override fun getLastProcessedSequence(): Long = lastSequence
    }

    private class MockUnbezahlteRechnungenRepository : UnbezahlteRechnungenRepository {
        val bezahtMarkiert = mutableListOf<Rechnungsnummer>()
        override fun save(rechnung: UnbezahlteRechnung) {}
        override fun findByRechnungsnummer(rechnungsnummer: Rechnungsnummer): UnbezahlteRechnung? = null
        override fun findUnbezahlteAb(datum: Rechnungsdatum): List<UnbezahlteRechnung> = emptyList()
        override fun markiereAlsBezahlt(rechnungsnummer: Rechnungsnummer) { bezahtMarkiert.add(rechnungsnummer) }
        override fun istAlsBezahltMarkiert(rechnungsnummer: Rechnungsnummer): Boolean = bezahtMarkiert.contains(rechnungsnummer)
    }

    @Test
    fun `verarbeitet Events nur einmal`() {
        val kontoumsatzRepository = MockKontoumsatzRepository()
        val processedEventRepository = MockProcessedEventRepository()
        val unbezahlteRechnungenRepository = MockUnbezahlteRechnungenRepository()
        val readModel = ZahlungsClearingReadModel(unbezahlteRechnungenRepository)
        val generator = object : EventSequenceGenerator {
            private var lastSequence = 0L
            override fun nextSequence(): Long = ++lastSequence
        }
        val service = ZahlungsMatcherService(kontoumsatzRepository, ZahlungsMatcher(generator), readModel, processedEventRepository)

        val event = KontoumsatzImportiert(
            buchungsreferenz = Buchungsreferenz("REF1"),
            properties = KontoumsatzProperties(
                buchungsdatum = Buchungsdatum(Datum(LocalDate.now())),
                partnername = Partnername("Max"),
                partnerIban = null,
                betrag = Kontoumsatzbetrag(Betrag(BigDecimal.TEN)),
                buchungsdetails = Buchungsdetails("RN_123"),
                buchungsreferenz = Buchungsreferenz("REF1"),
                zahlungsreferenz = null,
                mandatsId = null
            ),
            sequenznummer = generator.nextSequence()
        )
        kontoumsatzRepository.speichereEvents(listOf(event))

        // Erster Aufruf
        val ergebnisse1 = service.verarbeiteNeueEvents()
        assertEquals(1, ergebnisse1.size)
        assertEquals(1, unbezahlteRechnungenRepository.bezahtMarkiert.size)

        // Zweiter Aufruf
        val ergebnisse2 = service.verarbeiteNeueEvents()
        assertEquals(0, ergebnisse2.size)
        assertEquals(1, unbezahlteRechnungenRepository.bezahtMarkiert.size) // Immer noch 1
    }

    @Test
    fun `verarbeitet Events nicht erneut nach Neustart des Services`() {
        val kontoumsatzRepository = MockKontoumsatzRepository()
        val processedEventRepository = MockProcessedEventRepository()
        val unbezahlteRechnungenRepository = MockUnbezahlteRechnungenRepository()
        val readModel = ZahlungsClearingReadModel(unbezahlteRechnungenRepository)
        val generator = object : EventSequenceGenerator {
            private var lastSequence = 0L
            override fun nextSequence(): Long = ++lastSequence
        }
        
        val service1 = ZahlungsMatcherService(kontoumsatzRepository, ZahlungsMatcher(generator), readModel, processedEventRepository)

        val event = KontoumsatzImportiert(
            buchungsreferenz = Buchungsreferenz("REF1"),
            properties = KontoumsatzProperties(
                buchungsdatum = Buchungsdatum(Datum(LocalDate.now())),
                partnername = Partnername("Max"),
                partnerIban = null,
                betrag = Kontoumsatzbetrag(Betrag(BigDecimal.TEN)),
                buchungsdetails = Buchungsdetails("RN_123"),
                buchungsreferenz = Buchungsreferenz("REF1"),
                zahlungsreferenz = null,
                mandatsId = null
            ),
            sequenznummer = generator.nextSequence()
        )
        kontoumsatzRepository.speichereEvents(listOf(event))

        // Erster Service verarbeitet
        service1.verarbeiteNeueEvents()
        assertEquals(1, unbezahlteRechnungenRepository.bezahtMarkiert.size)

        // Neuer Service mit gleichem (persistenten) Repository
        val service2 = ZahlungsMatcherService(kontoumsatzRepository, ZahlungsMatcher(generator), readModel, processedEventRepository)
        val ergebnisse = service2.verarbeiteNeueEvents()
        
        assertEquals(0, ergebnisse.size, "Sollte keine neuen Events finden, da sie bereits verarbeitet wurden")
        assertEquals(1, unbezahlteRechnungenRepository.bezahtMarkiert.size)
    }
}
