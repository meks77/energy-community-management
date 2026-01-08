package at.hillstrom.energy.specs

import at.hillstrom.energy.*
import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.usecases.importe.kontoumsatzimport.KontoumsatzRepository
import at.hillstrom.energy.usecases.importe.mitgliederimport.MitgliedRepository
import at.hillstrom.energy.usecases.importe.rechnungsimport.RechnungRepository
import at.hillstrom.energy.usecases.unbezahlterechnungen.UnbezahlteRechnung
import at.hillstrom.energy.usecases.unbezahlterechnungen.UnbezahlteRechnungenRepository as UnbezahlteRechnungenRepositoryInterface
import io.cucumber.java.Before
import java.util.*

class SharedAppContext {
    private val _mitgliedRepository = InMemoryMitgliedRepository()
    val mitgliedRepository: MitgliedRepository get() = _mitgliedRepository

    private val _rechnungRepository = InMemoryRechnungRepository()

    private val _kontoumsatzRepository = InMemoryKontoumsatzRepository()

    private val _importRepository = InMemoryImportRepository()

    private val _unbezahlteRechnungenRepository = InMemoryUnbezahlteRechnungenRepository()

    lateinit var app: App

    @Before
    fun setup() {
        app = App(
            mitgliedRepository = _mitgliedRepository,
            rechnungRepository = _rechnungRepository,
            kontoumsatzRepository = _kontoumsatzRepository,
            importRepository = _importRepository,
            unbezahlteRechnungenRepository = _unbezahlteRechnungenRepository
        )
    }

    // Helfer für Assertions in den Steps
    fun getMitgliederEvents(): Map<Mitgliedsnummer, List<MitgliedEvent>> = _mitgliedRepository.storage
    fun getRechnungEvents(): List<RechnungEvent> = _rechnungRepository.storage.values.flatten()
    fun getKontoumsatzEvents(): List<KontoumsatzEvent> = _kontoumsatzRepository.storage.values.flatten()
}

private class InMemoryMitgliedRepository : MitgliedRepository {
    val storage = mutableMapOf<Mitgliedsnummer, MutableList<MitgliedEvent>>()
    override fun ladeEvents(kundennummer: Mitgliedsnummer): List<MitgliedEvent> = storage[kundennummer] ?: emptyList()
    override fun speichereEvents(events: List<MitgliedEvent>) {
        events.forEach { event ->
            storage.computeIfAbsent(event.kundennummer) { mutableListOf() }.add(event)
        }
    }
}

private class InMemoryRechnungRepository : RechnungRepository {
    val storage = mutableMapOf<UUID, MutableList<RechnungEvent>>()
    override fun ladeEvents(id: UUID): List<RechnungEvent> = storage[id] ?: emptyList()
    override fun speichereEvents(events: List<RechnungEvent>) {
        events.forEach { event ->
            storage.computeIfAbsent(event.id) { mutableListOf() }.add(event)
        }
    }
}

private class InMemoryKontoumsatzRepository : KontoumsatzRepository {
    val storage = mutableMapOf<Buchungsreferenz, MutableList<KontoumsatzEvent>>()
    override fun ladeEvents(buchungsreferenz: Buchungsreferenz): List<KontoumsatzEvent> = storage[buchungsreferenz] ?: emptyList()
    override fun speichereEvents(events: List<KontoumsatzEvent>) {
        events.forEach { event ->
            storage.computeIfAbsent(event.buchungsreferenz) { mutableListOf() }.add(event)
        }
    }
}

private class InMemoryImportRepository : ImportRepository {
    val events = mutableListOf<ImportEvent>()
    override fun speichereImportEvent(event: ImportEvent) {
        events.add(event)
    }
}

private class InMemoryUnbezahlteRechnungenRepository : UnbezahlteRechnungenRepositoryInterface {
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
