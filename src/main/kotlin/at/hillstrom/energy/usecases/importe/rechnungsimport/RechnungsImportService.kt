package at.hillstrom.energy.usecases.importe.rechnungsimport

import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.RechnungErstellt
import at.hillstrom.energy.RechnungenImportErfolgreich
import at.hillstrom.energy.RechnungenImportFehlgeschlagen

class RechnungsImportService(
    private val repository: RechnungRepository,
    private val importRepository: ImportRepository
) {

    fun importiere(source: RechnungImportSource) {
        try {
            val allNewEvents = mutableListOf<RechnungErstellt>()
            while (source.hasNext()) {
                val properties = source.next()
                val id = Rechnung.erstelleRechnung(properties).id
                val existingEvent = repository.finde(id)
                
                if (existingEvent == null) {
                    allNewEvents.add(Rechnung.erstelleRechnung(properties))
                } else {
                    val rechnung = Rechnung(existingEvent)
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
