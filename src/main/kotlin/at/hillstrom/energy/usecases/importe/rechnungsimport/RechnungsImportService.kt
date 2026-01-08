package at.hillstrom.energy.usecases.importe.rechnungsimport

import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.RechnungErstellt
import at.hillstrom.energy.RechnungenImportErfolgreich
import at.hillstrom.energy.RechnungenImportFehlgeschlagen
import at.hillstrom.energy.EventSequenceGenerator

class RechnungsImportService(
    private val repository: RechnungRepository,
    private val importRepository: ImportRepository,
    private val sequenceGenerator: EventSequenceGenerator
) {

    fun importiere(source: RechnungImportSource) {
        try {
            val allNewEvents = mutableListOf<RechnungErstellt>()
            while (source.hasNext()) {
                val properties = source.next()
                val tempRechnung = Rechnung.erstelleRechnung(properties, 0) // nur für ID
                val id = tempRechnung.id
                val existingEvent = repository.finde(id)
                
                if (existingEvent == null) {
                    allNewEvents.add(Rechnung.erstelleRechnung(properties, sequenceGenerator.nextSequence()))
                } else {
                    val rechnung = Rechnung(existingEvent)
                    rechnung.validiereGleichheit(properties)
                }
            }

            repository.speichereEvents(allNewEvents)
            importRepository.speichereImportEvent(RechnungenImportErfolgreich(sequenznummer = sequenceGenerator.nextSequence()))
        } catch (e: Exception) {
            importRepository.speichereImportEvent(RechnungenImportFehlgeschlagen(e.message ?: "Unbekannter Fehler", sequenznummer = sequenceGenerator.nextSequence()))
            throw e
        }
    }
}
