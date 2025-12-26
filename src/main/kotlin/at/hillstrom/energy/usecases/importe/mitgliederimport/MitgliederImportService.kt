package at.hillstrom.energy.usecases.importe.mitgliederimport

import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.MitgliedEvent
import at.hillstrom.energy.MitgliederImportErfolgreich
import at.hillstrom.energy.MitgliederImportFehlgeschlagen

class MitgliederImportService(
    private val repository: MitgliedRepository,
    private val importRepository: ImportRepository
) {

    fun importiere(source: MitgliedImportSource) {
        try {
            val allNewEvents = mutableListOf<MitgliedEvent>()
            while (source.hasNext()) {
                val (kundennummer, properties) = source.next()
                val existingEvents = repository.ladeEvents(kundennummer)
                val newEvents = if (existingEvents.isEmpty()) {
                    listOf(Mitglied.erstelleMitglied(kundennummer, properties))
                } else {
                    val mitglied = Mitglied.fromEvents(existingEvents)
                    mitglied.aktualisiereMitglied(properties)
                }
                allNewEvents.addAll(newEvents)
            }
            
            repository.speichereEvents(allNewEvents)
            importRepository.speichereImportEvent(MitgliederImportErfolgreich())
        } catch (e: Exception) {
            importRepository.speichereImportEvent(MitgliederImportFehlgeschlagen(e.message ?: "Unbekannter Fehler"))
            throw e
        }
    }

}
