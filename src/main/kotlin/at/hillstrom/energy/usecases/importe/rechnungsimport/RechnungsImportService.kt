package at.hillstrom.energy.usecases.importe.rechnungsimport

import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.RechnungErstellt
import at.hillstrom.energy.RechnungEvent
import at.hillstrom.energy.RechnungenImportErfolgreich
import at.hillstrom.energy.RechnungenImportFehlgeschlagen

class RechnungsImportService(
    private val repository: RechnungRepository,
    private val importRepository: ImportRepository
) {

    fun importiere(source: RechnungImportSource) {
        try {
            val allNewEvents = mutableListOf<RechnungEvent>()
            while (source.hasNext()) {
                val properties = source.next()
                val id = Rechnung.erstelleRechnung(properties).id
                val existingEvents = repository.ladeEvents(id)
                
                if (existingEvents.isEmpty()) {
                    allNewEvents.add(Rechnung.erstelleRechnung(properties))
                } else {
                    val rechnung = Rechnung(existingEvents.filterIsInstance<RechnungErstellt>().first())
                    rechnung.validiereGleichheit(properties)
                }
            }

            repository.speichereEvents(allNewEvents)
            importRepository.speichereImportEvent(RechnungenImportErfolgreich())
        } catch (e: Exception) {
            importRepository.speichereImportEvent(RechnungenImportFehlgeschlagen(e.message ?: "Unbekannter Fehler"))
            throw e
        }
    }
}
