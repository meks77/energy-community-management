package at.hillstrom.energy.usecases.mitgliederimport

import at.hillstrom.energy.*
import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.usecases.importe.mitgliederimport.MitgliedImportSource
import at.hillstrom.energy.usecases.importe.mitgliederimport.MitgliedRepository
import at.hillstrom.energy.usecases.importe.mitgliederimport.MitgliederImportService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MitgliederImportServiceTest {

    private val kundennummer = Mitgliedsnummer("K12345")
    private val initialAdresse = Adresse(Strasse("Musterstraße"), Hausnummer("1"), PLZ("1234"), Ort("Musterstadt"))
    private val properties = MitgliedProperties(
        Name("Max Mustermann"),
        initialAdresse,
        Email("max@mustermann.de"),
        Steuerklasse.PRIVAT
    )

    private class MockRepository : MitgliedRepository {
        val storedEvents = mutableListOf<MitgliedEvent>()
        val loadedEvents = mutableListOf<MitgliedEvent>()

        override fun ladeEvents(kundennummer: Mitgliedsnummer): List<MitgliedEvent> {
            return loadedEvents.filter { it.kundennummer == kundennummer }
        }

        override fun speichereEvents(events: List<MitgliedEvent>) {
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

    private class ListSource(items: List<Pair<Mitgliedsnummer, MitgliedProperties>>) : MitgliedImportSource {
        private val iterator = items.iterator()
        override fun hasNext(): Boolean = iterator.hasNext()
        override fun next(): Pair<Mitgliedsnummer, MitgliedProperties> = iterator.next()
    }

    private class SimpleEventSequenceGenerator(initialValue: Long = 0) : EventSequenceGenerator {
        private var lastSequence = initialValue
        override fun nextSequence(): Long = ++lastSequence
    }

    @Test
    fun `importiere verarbeitet Liste erfolgreich`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = MitgliederImportService(repo, importRepo, SimpleEventSequenceGenerator())
        val source = ListSource(listOf(kundennummer to properties))

        service.importiere(source)

        assertEquals(1, repo.storedEvents.size)
        assertTrue(importRepo.importEvents.any { it is MitgliederImportErfolgreich })
    }

    @Test
    fun `importiere speichert Fehler-Event bei Exception`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = MitgliederImportService(repo, importRepo, SimpleEventSequenceGenerator())
        val source = object : MitgliedImportSource {
            override fun hasNext() = true
            override fun next() = throw RuntimeException("Test-Fehler")
        }

        try {
            service.importiere(source)
        } catch (e: RuntimeException) {
            assertEquals("Test-Fehler", e.message)
        }

        assertEquals(0, repo.storedEvents.size)
        assertTrue(importRepo.importEvents.any { it is MitgliederImportFehlgeschlagen && it.fehler == "Test-Fehler" })
    }

    @Test
    fun `importiert neues Mitglied`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = MitgliederImportService(repo, importRepo, SimpleEventSequenceGenerator())
        val source = ListSource(listOf(kundennummer to properties))

        service.importiere(source)

        assertEquals(1, repo.storedEvents.size)
        val event = repo.storedEvents[0] as MitgliedAngelegt
        assertEquals(kundennummer, event.kundennummer)
        assertEquals(properties.name, event.name)
    }

    @Test
    fun `aktualisiert bestehendes Mitglied`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = MitgliederImportService(repo, importRepo, SimpleEventSequenceGenerator())

        // Zuerst anlegen
        service.importiere(ListSource(listOf(kundennummer to properties)))
        repo.storedEvents.clear()

        // Dann aktualisieren
        val neueProperties = properties.copy(name = Name("Neuer Name"))
        service.importiere(ListSource(listOf(kundennummer to neueProperties)))

        assertEquals(1, repo.storedEvents.size)
        assertTrue(repo.storedEvents[0] is NameGeaendert)
        assertEquals(Name("Neuer Name"), (repo.storedEvents[0] as NameGeaendert).neuerName)
    }

    @Test
    fun `erzeugt keine Events wenn keine Aenderung vorliegt`() {
        val repo = MockRepository()
        val importRepo = MockImportRepository()
        val service = MitgliederImportService(repo, importRepo, SimpleEventSequenceGenerator())

        // Zuerst anlegen
        service.importiere(ListSource(listOf(kundennummer to properties)))
        repo.storedEvents.clear()

        // Erneut mit gleichen Daten importieren
        service.importiere(ListSource(listOf(kundennummer to properties)))

        assertEquals(0, repo.storedEvents.size)
    }
}
