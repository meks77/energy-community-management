package at.hillstrom.energy

import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.usecases.importe.kontoumsatzimport.KontoumsatzRepository
import at.hillstrom.energy.usecases.importe.mitgliederimport.MitgliedRepository
import at.hillstrom.energy.usecases.importe.rechnungsimport.RechnungRepository
import at.hillstrom.energy.usecases.unbezahlterechnungen.ProcessedEventRepository
import at.hillstrom.energy.usecases.unbezahlterechnungen.UnbezahlteRechnung
import at.hillstrom.energy.usecases.unbezahlterechnungen.UnbezahlteRechnungenRepository
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class AppTest {

    @Test
    fun `App kann initialisiert werden`() {
        val mitgliedRepository = object : MitgliedRepository {
            override fun ladeEvents(kundennummer: Mitgliedsnummer): List<MitgliedEvent> = emptyList()
            override fun speichereEvents(events: List<MitgliedEvent>) {}
        }
        val rechnungRepository = object : RechnungRepository {
            override fun finde(id: Rechnungsnummer): RechnungErstellt? = null
            override fun speichereEvents(events: List<RechnungErstellt>) {}
        }
        val kontoumsatzRepository = object : KontoumsatzRepository {
            override fun ladeEvents(buchungsreferenz: Buchungsreferenz): List<KontoumsatzEvent> = emptyList()
            override fun speichereEvents(events: List<KontoumsatzEvent>) {}
            override fun alleLaden(): List<KontoumsatzEvent> = emptyList()
            override fun ladeAbSequenz(sequenznummer: Long): List<KontoumsatzEvent> = emptyList()
        }
        val importRepository = object : ImportRepository {
            override fun speichereImportEvent(event: ImportEvent) {}
        }
        val unbezahlteRechnungenRepository = object : UnbezahlteRechnungenRepository {
            override fun save(rechnung: UnbezahlteRechnung) {}
            override fun findByRechnungsnummer(rechnungsnummer: Rechnungsnummer): UnbezahlteRechnung? = null
            override fun findUnbezahlteAb(datum: Rechnungsdatum): List<UnbezahlteRechnung> = emptyList()
            override fun markiereAlsBezahlt(rechnungsnummer: Rechnungsnummer) {}
            override fun istAlsBezahltMarkiert(rechnungsnummer: Rechnungsnummer): Boolean = false
        }
        val processedEventRepository = object : ProcessedEventRepository {
            override fun saveLastProcessedSequence(sequence: Long) {}
            override fun getLastProcessedSequence(): Long = 0
        }

        val app = App(
            mitgliedRepository,
            rechnungRepository,
            kontoumsatzRepository,
            importRepository,
            unbezahlteRechnungenRepository,
            processedEventRepository,
            object : EventSequenceGenerator {
                private var lastSequence = 0L
                override fun nextSequence(): Long = ++lastSequence
            }
        )

        assertNotNull(app.mitgliederImportService)
        assertNotNull(app.rechnungsImportService)
        assertNotNull(app.kontoumsatzImportService)
        assertNotNull(app.zahlungsMatcherService)
        assertNotNull(app.zahlungsClearingReadModel)
    }
}
