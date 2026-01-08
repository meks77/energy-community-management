package at.hillstrom.energy.specs.kontoumsatzimport

import at.hillstrom.energy.*
import at.hillstrom.energy.specs.SharedAppContext
import at.hillstrom.energy.usecases.importe.kontoumsatzimport.*
import io.cucumber.java.de.Angenommen
import io.cucumber.java.de.Gegebenseien
import io.cucumber.java.de.Wenn
import io.cucumber.java.de.Dann
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals

class KontoumsatzImportSteps(private val context: SharedAppContext) {

    private val umsaetzeInSource = mutableListOf<KontoumsatzProperties>()
    private var lastException: Exception? = null

    private val app get() = context.app

    @Gegebenseien("folgende Kontoumsätze in der Import-Quelle:")
    fun seienFolgendeKontoumsaetzeInDerImportQuelle(rows: List<Map<String, String>>) {
        rows.forEach { row ->
            umsaetzeInSource.add(mapRowToProperties(row))
        }
    }

    @Angenommen("folgende Kontoumsätze wurden bereits importiert:")
    fun seienFolgendeKontoumsaetzeBereitsImportiert(rows: List<Map<String, String>>) {
        rows.forEach { row ->
            val properties = mapRowToProperties(row)
            app.kontoumsatzRepository.speichereEvents(listOf(Kontoumsatz.importiereUmsatz(properties, app.sequenceGenerator.nextSequence())))
        }
    }

    private fun mapRowToProperties(row: Map<String, String>): KontoumsatzProperties {
        return KontoumsatzProperties(
            Buchungsdatum(Datum(LocalDate.parse(row["Datum"]!!))),
            Partnername(row["Partner"]!!),
            null,
            Kontoumsatzbetrag(Betrag(BigDecimal(row["Betrag"]!!))),
            null,
            Buchungsreferenz(row["Referenz"]!!),
            null,
            null
        )
    }

    @Wenn("der Kontoumsatz-Import ausgeführt wird")
    fun der_kontoumsatz_import_ausgefuehrt_wird() {
        val source = object : KontoumsatzImportSource {
            private val iterator = umsaetzeInSource.iterator()
            override fun hasNext(): Boolean = iterator.hasNext()
            override fun next(): KontoumsatzProperties = iterator.next()
        }
        try {
            app.kontoumsatzImportService.importiere(source)
        } catch (e: Exception) {
            lastException = e
        }
    }

    @Dann("gab es einen Fehler beim Kontoumsatz-Import")
    fun gab_es_einen_fehler_beim_kontoumsatz_import() {
        kotlin.test.assertNotNull(lastException, "Es wurde ein Fehler erwartet, aber es trat keiner auf")
    }

    @Dann("sind folgende Kontoumsätze im System vorhanden:")
    fun sind_folgende_kontoumsaetze_im_system_vorhanden(expectedRows: List<Map<String, String>>) {
        val actualUmsaetze = context.getKontoumsatzEvents()
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
