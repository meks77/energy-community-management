package at.hillstrom.energy.specs.rechnungsimport

import at.hillstrom.energy.*
import at.hillstrom.energy.specs.SharedAppContext
import at.hillstrom.energy.usecases.importe.rechnungsimport.RechnungImportSource
import io.cucumber.java.de.Angenommen
import io.cucumber.java.de.Dann
import io.cucumber.java.de.Wenn
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals

class RechnungImportSteps(private val context: SharedAppContext) {

    private val rechnungenInSource = mutableListOf<RechnungProperties>()

    private val app get() = context.app

    @Angenommen("folgende Rechnungen sind in der Import-Quelle:")
    fun seienFolgendeRechnungenInDerImportQuelle(rows: List<Map<String, String>>) {
        rows.forEach { row ->
            val bruttobetrag = BigDecimal(row["Bruttobetrag"]!!)
            val datum = Datum(LocalDate.parse(row["Datum"]!!))
            val steuerklasse = RechnungsSteuerklasse.valueOf(row["Steuerklasse"]!!)
            
            rechnungenInSource.add(RechnungProperties(
                Mitgliedsnummer(row["Mitgliedsnummer"]!!),
                Nettobetrag(Betrag(bruttobetrag)), // Vereinfacht für Test
                Bruttobetrag(Betrag(bruttobetrag)),
                Umsatzsteuer(Betrag(BigDecimal.ZERO)),
                Rechnungsnummer(row["Rechnungsnummer"]!!),
                Rechnungsdatum(datum),
                Faelligkeitsdatum(datum),
                steuerklasse
            ))
        }
    }

    @Wenn("der Rechnungs-Import ausgeführt wird")
    fun der_rechnungs_import_ausgefuehrt_wird() {
        val source = object : RechnungImportSource {
            private val iterator = rechnungenInSource.iterator()
            override fun hasNext(): Boolean = iterator.hasNext()
            override fun next(): RechnungProperties = iterator.next()
        }
        app.rechnungsImportService.importiere(source)
    }

    @Dann("sind folgende Rechnungen im System vorhanden:")
    fun sind_folgende_rechnungen_im_system_vorhanden(expectedRows: List<Map<String, String>>) {
        val actualRechnungen = context.getRechnungEvents()
            .filterIsInstance<RechnungErstellt>()
            .map { it.properties }
            .sortedBy { it.rechnungsnummer.wert }

        assertEquals(expectedRows.size, actualRechnungen.size, "Anzahl der Rechnungen stimmt nicht überein")

        expectedRows.sortedBy { it["Rechnungsnummer"] }.forEachIndexed { index, expected ->
            val actual = actualRechnungen[index]
            assertEquals(expected["Mitgliedsnummer"], actual.mitgliedsnummer.wert)
            assertEquals(expected["Rechnungsnummer"], actual.rechnungsnummer.wert)
            assertEquals(expected["Datum"], actual.rechnungsdatum.datum.wert.toString())
            assertEquals(BigDecimal(expected["Bruttobetrag"]!!), actual.bruttobetrag.betrag.wert)
        }
    }
}
