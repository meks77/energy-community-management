package at.hillstrom.energy.usecases.rechnungsimport

import at.hillstrom.energy.*
import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.usecases.importe.rechnungsimport.RechnungImportSource
import at.hillstrom.energy.usecases.importe.rechnungsimport.RechnungRepository
import at.hillstrom.energy.usecases.importe.rechnungsimport.RechnungsImportService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate

class RechnungsImportServiceTest {

    private val properties = RechnungProperties(
        mitgliedsnummer = Mitgliedsnummer("M123"),
        nettobetrag = Nettobetrag(Betrag(BigDecimal("100.00"))),
        bruttobetrag = Bruttobetrag(Betrag(BigDecimal("120.00"))),
        umsatzsteuer = Umsatzsteuer(Betrag(BigDecimal("20.00"))),
        rechnungsnummer = Rechnungsnummer("R-2023-001"),
        rechnungsdatum = Rechnungsdatum(Datum(LocalDate.of(2023, 12, 23))),
        faelligkeitsdatum = Faelligkeitsdatum(Datum(LocalDate.of(2024, 1, 23))),
        steuerklasse = RechnungsSteuerklasse.UMSATZSTEUERPFLICHTIG
    )

    private class MockRepository : RechnungRepository {
        val storedEvents = mutableListOf<RechnungErstellt>()
        val loadedEvents = mutableListOf<RechnungErstellt>()

        override fun finde(id: Rechnungsnummer): RechnungErstellt? {
            return loadedEvents.find { it.id == id }
        }

        override fun speichereEvents(events: List<RechnungErstellt>) {
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

    private class ListSource(items: List<RechnungProperties>) : RechnungImportSource {
        private val iterator = items.iterator()
        override fun hasNext(): Boolean = iterator.hasNext()
        override fun next(): RechnungProperties = iterator.next()
    }

    @Test
    fun `importiere verarbeitet Liste erfolgreich`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = RechnungsImportService(repo, importRepo)
        val source = ListSource(listOf(properties))

        service.importiere(source)

        assertEquals(1, repo.storedEvents.size)
        assertTrue(importRepo.importEvents.any { it is RechnungenImportErfolgreich })
    }

    @Test
    fun `importiere speichert Fehler-Event bei Exception`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = RechnungsImportService(repo, importRepo)
        val source = object : RechnungImportSource {
            override fun hasNext() = true
            override fun next() = throw RuntimeException("Test-Fehler")
        }

        assertThrows<RuntimeException> {
            service.importiere(source)
        }

        assertEquals(0, repo.storedEvents.size)
        assertTrue(importRepo.importEvents.any { it is RechnungenImportFehlgeschlagen && it.fehler == "Test-Fehler" })
    }

    @Test
    fun `importiert neue Rechnung`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = RechnungsImportService(repo, importRepo)
        val source = ListSource(listOf(properties))

        service.importiere(source)

        assertEquals(1, repo.storedEvents.size)
        val event = repo.storedEvents[0]
        assertEquals(properties, event.properties)
    }

    @Test
    fun `erkennt Duplikate und importiert nicht erneut`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = RechnungsImportService(repo, importRepo)

        // Zuerst importieren
        service.importiere(ListSource(listOf(properties)))
        repo.storedEvents.clear()

        // Erneut mit gleichen Daten importieren
        service.importiere(ListSource(listOf(properties)))

        assertEquals(0, repo.storedEvents.size)
    }

    @Test
    fun `wirft Fehler bei abweichenden Daten fuer gleiche ID`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = RechnungsImportService(repo, importRepo)

        // Erster Import, welcher die Rechnung "anlegt"
        service.importiere(ListSource(listOf(properties)))

        val abweichendeProperties = properties.copy(bruttobetrag = Bruttobetrag(Betrag(BigDecimal(121.0))))
        // Erneuter Import derselben Rechnung mit abweichenden Daten
        assertThrows<IllegalArgumentException> {
            service.importiere(ListSource(listOf(abweichendeProperties)))
        }
        
        assertTrue(importRepo.importEvents.any { it is RechnungenImportFehlgeschlagen })
    }
}
