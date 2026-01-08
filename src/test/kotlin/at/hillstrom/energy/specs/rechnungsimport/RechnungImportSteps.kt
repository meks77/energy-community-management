package at.hillstrom.energy.specs.rechnungsimport

import at.hillstrom.energy.*
import at.hillstrom.energy.specs.SharedAppContext
import at.hillstrom.energy.usecases.importe.rechnungsimport.Rechnung
import at.hillstrom.energy.usecases.importe.rechnungsimport.RechnungImportSource
import io.cucumber.java.de.Angenommen
import io.cucumber.java.de.Dann
import io.cucumber.java.de.Wenn
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals

class RechnungImportSteps(private val context: SharedAppContext) {

    private val rechnungenInSource = mutableListOf<RechnungProperties>()
    private var lastException: Exception? = null

    private val app get() = context.app

    @Angenommen("folgende Rechnungen wurden bereits importiert:")
    fun seienFolgendeRechnungenBereitsImportiert(rows: List<Map<String, String>>) {
        rows.forEach { row ->
            val bruttobetrag = BigDecimal(row["Bruttobetrag"]!!)
            val datum = Datum(LocalDate.parse(row["Datum"]!!))
            val steuerklasse = RechnungsSteuerklasse.valueOf(row["Steuerklasse"]!!)

            val properties = RechnungProperties(
                Mitgliedsnummer(row["Mitgliedsnummer"]!!),
                Nettobetrag(Betrag(bruttobetrag)),
                Bruttobetrag(Betrag(bruttobetrag)),
                Umsatzsteuer(Betrag(BigDecimal.ZERO)),
                Rechnungsnummer(row["Rechnungsnummer"]!!),
                Rechnungsdatum(datum),
                Faelligkeitsdatum(datum),
                steuerklasse
            )
            app.rechnungRepository.speichereEvents(listOf(Rechnung.erstelleRechnung(properties)))
        }
    }

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
        try {
            app.rechnungsImportService.importiere(source)
        } catch (e: Exception) {
            lastException = e
        }
    }

    @Dann("gab es einen Fehler beim Rechnungs-Import")
    fun gab_es_einen_fehler_beim_rechnungs_import() {
        kotlin.test.assertNotNull(lastException, "Es wurde ein Fehler erwartet, aber es trat keiner auf")
    }

    @Dann("sind folgende Rechnungen im System vorhanden:")
    fun sind_folgende_rechnungen_im_system_vorhanden(expectedRows: List<Map<String, String>>) {
        val actualRechnungen = context.getRechnungEvents()
            .map { Rechnung(it).properties }
            .sortedBy { it.rechnungsnummer.wert }

        assertEquals(expectedRows.size, actualRechnungen.size, "Anzahl der Rechnungen stimmt nicht überein")

        expectedRows.sortedBy { it["Rechnungsnummer"] }.forEachIndexed { index, expected ->
            val actual = actualRechnungen[index]
            expected["Mitgliedsnummer"]?.let { assertEquals(it, actual.mitgliedsnummer.wert, "Mitgliedsnummer stimmt nicht überein") }
            expected["Rechnungsnummer"]?.let { assertEquals(it, actual.rechnungsnummer.wert, "Rechnungsnummer stimmt nicht überein") }
            expected["Datum"]?.let { assertEquals(it, actual.rechnungsdatum.datum.wert.toString(), "Datum stimmt nicht überein") }
            expected["Bruttobetrag"]?.let { assertEquals(BigDecimal(it), actual.bruttobetrag.betrag.wert, "Bruttobetrag stimmt nicht überein") }
            expected["Steuerklasse"]?.let { assertEquals(it, actual.steuerklasse.name, "Steuerklasse stimmt nicht überein") }
        }
    }
}
