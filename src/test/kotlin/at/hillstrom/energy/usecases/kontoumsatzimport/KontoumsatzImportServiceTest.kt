package at.hillstrom.energy.usecases.kontoumsatzimport

import at.hillstrom.energy.*
import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.usecases.importe.kontoumsatzimport.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate

class KontoumsatzImportServiceTest {

    private val properties = KontoumsatzProperties(
        buchungsdatum = Buchungsdatum(Datum(LocalDate.of(2023, 12, 23))),
        partnername = Partnername("Max Mustermann"),
        partnerIban = IBAN("AT123456789012345678"),
        betrag = Kontoumsatzbetrag(Betrag(BigDecimal("100.00"))),
        buchungsdetails = Buchungsdetails("Mitgliedsbeitrag"),
        buchungsreferenz = Buchungsreferenz("REF123"),
        zahlungsreferenz = Zahlungsreferenz("ZAL123"),
        mandatsId = MandatsId("MANDAT123")
    )

    private class MockRepository : KontoumsatzRepository {
        val storedEvents = mutableListOf<KontoumsatzEvent>()
        val loadedEvents = mutableListOf<KontoumsatzEvent>()

        override fun ladeEvents(buchungsreferenz: Buchungsreferenz): List<KontoumsatzEvent> {
            return loadedEvents.filter { it.buchungsreferenz == buchungsreferenz }
        }

        override fun speichereEvents(events: List<KontoumsatzEvent>) {
            storedEvents.addAll(events)
            loadedEvents.addAll(events)
        }
    }

    private class MockImportRepository : ImportRepository {
        val importEvents = mutableListOf<ImportEvent>()
        override fun speichereImportEvent(event: ImportEvent) {
            importEvents.add(event)
        }
    }

    private class ListSource(val items: List<KontoumsatzProperties>) : KontoumsatzImportSource {
        private val iterator = items.iterator()
        override fun hasNext(): Boolean = iterator.hasNext()
        override fun next(): KontoumsatzProperties = iterator.next()
    }

    @Test
    fun `importiere verarbeitet Liste erfolgreich`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = KontoumsatzImportService(repo, importRepo)
        val source = ListSource(listOf(properties))

        service.importiere(source)

        assertEquals(1, repo.storedEvents.size)
        assertTrue(importRepo.importEvents.any { it is KontoumsaetzeImportErfolgreich })
    }

    @Test
    fun `importiere speichert Fehler-Event bei Exception`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = KontoumsatzImportService(repo, importRepo)
        val source = object : KontoumsatzImportSource {
            override fun hasNext() = true
            override fun next() = throw RuntimeException("Test-Fehler")
        }

        assertThrows<RuntimeException> {
            service.importiere(source)
        }

        assertEquals(0, repo.storedEvents.size)
        assertTrue(importRepo.importEvents.any { it is KontoumsaetzeImportFehlgeschlagen && it.fehler == "Test-Fehler" })
    }

    @Test
    fun `importiert neuen Umsatz`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = KontoumsatzImportService(repo, importRepo)
        val source = ListSource(listOf(properties))

        service.importiere(source)

        assertEquals(1, repo.storedEvents.size)
        val event = repo.storedEvents[0] as KontoumsatzImportiert
        assertEquals(properties, event.properties)
    }

    @Test
    fun `erkennt Duplikate und importiert nicht erneut`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = KontoumsatzImportService(repo, importRepo)

        // Zuerst importieren
        service.importiere(ListSource(listOf(properties)))
        repo.storedEvents.clear()

        // Erneut mit gleichen Daten importieren
        service.importiere(ListSource(listOf(properties)))

        assertEquals(0, repo.storedEvents.size)
    }

    @Test
    fun `wirft Fehler bei abweichenden Daten fuer gleiche Buchungsreferenz`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = KontoumsatzImportService(repo, importRepo)

        // Zuerst importieren
        service.importiere(ListSource(listOf(properties)))
        repo.storedEvents.clear()

        // Abweichende Partnername (Referenz bleibt gleich)
        val abweichendeProperties = properties.copy(partnername = Partnername("Anderer Name"))
        
        assertThrows<IllegalArgumentException> {
            service.importiere(ListSource(listOf(abweichendeProperties)))
        }
        
        assertTrue(importRepo.importEvents.any { it is KontoumsaetzeImportFehlgeschlagen })
    }
}
