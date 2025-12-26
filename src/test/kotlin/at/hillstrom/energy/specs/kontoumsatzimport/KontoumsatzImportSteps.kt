package at.hillstrom.energy.specs.kontoumsatzimport

import at.hillstrom.energy.*
import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.usecases.importe.kontoumsatzimport.*
import io.cucumber.java.de.Gegebenseien
import io.cucumber.java.de.Wenn
import io.cucumber.java.de.Dann
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals

class KontoumsatzImportSteps {

    private val umsaetzeInSource = mutableListOf<KontoumsatzProperties>()

    private val repository = object : KontoumsatzRepository {
        val storage = mutableMapOf<Buchungsreferenz, MutableList<KontoumsatzEvent>>()
        override fun ladeEvents(buchungsreferenz: Buchungsreferenz): List<KontoumsatzEvent> = storage[buchungsreferenz] ?: emptyList()
        override fun speichereEvents(events: List<KontoumsatzEvent>) {
            events.forEach { event ->
                storage.computeIfAbsent(event.buchungsreferenz) { mutableListOf() }.add(event)
            }
        }
    }

    private val importRepository = object : ImportRepository {
        override fun speichereImportEvent(event: ImportEvent) {}
    }

    @Gegebenseien("folgende Kontoumsätze in der Import-Quelle:")
    fun seienFolgendeKontoumsaetzeInDerImportQuelle(rows: List<Map<String, String>>) {
        rows.forEach { row ->
            umsaetzeInSource.add(KontoumsatzProperties(
                Buchungsdatum(Datum(LocalDate.parse(row["Datum"]!!))),
                Partnername(row["Partner"]!!),
                null,
                Kontoumsatzbetrag(Betrag(BigDecimal(row["Betrag"]!!))),
                null,
                Buchungsreferenz(row["Referenz"]!!),
                null,
                null
            ))
        }
    }

    @Wenn("der Kontoumsatz-Import ausgeführt wird")
    fun der_kontoumsatz_import_ausgefuehrt_wird() {
        val source = object : KontoumsatzImportSource {
            private val iterator = umsaetzeInSource.iterator()
            override fun hasNext(): Boolean = iterator.hasNext()
            override fun next(): KontoumsatzProperties = iterator.next()
        }
        val service = KontoumsatzImportService(repository, importRepository)
        service.importiere(source)
    }

    @Dann("sind folgende Kontoumsätze im System vorhanden:")
    fun sind_folgende_kontoumsaetze_im_system_vorhanden(expectedRows: List<Map<String, String>>) {
        val actualUmsaetze = repository.storage.values.flatten()
            .filterIsInstance<KontoumsatzImportiert>()
            .map { it.properties }
            .sortedBy { it.buchungsreferenz.wert }

        assertEquals(expectedRows.size, actualUmsaetze.size, "Anzahl der Kontoumsätze stimmt nicht überein")

        expectedRows.sortedBy { it["Referenz"] }.forEachIndexed { index, expected ->
            val actual = actualUmsaetze[index]
            assertEquals(expected["Datum"], actual.buchungsdatum.wert.wert.toString())
            assertEquals(expected["Partner"], actual.partnername.wert)
            assertEquals(BigDecimal(expected["Betrag"]!!), actual.betrag.wert.wert)
            assertEquals(expected["Referenz"], actual.buchungsreferenz.wert)
        }
    }
}
