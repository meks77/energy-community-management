package at.hillstrom.energy.usecases.importe.mitgliederimport

import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.MitgliedEvent
import at.hillstrom.energy.MitgliederImportErfolgreich
import at.hillstrom.energy.MitgliederImportFehlgeschlagen
import at.hillstrom.energy.EventSequenceGenerator

class MitgliederImportService(
    private val repository: MitgliedRepository,
    private val importRepository: ImportRepository,
    private val sequenceGenerator: EventSequenceGenerator
) {

    fun importiere(source: MitgliedImportSource) {
        try {
            val allNewEvents = mutableListOf<MitgliedEvent>()
            while (source.hasNext()) {
                val (kundennummer, properties) = source.next()
                val existingEvents = repository.ladeEvents(kundennummer)
                val newEvents = if (existingEvents.isEmpty()) {
                    listOf(Mitglied.erstelleMitglied(kundennummer, properties, sequenceGenerator.nextSequence()))
                } else {
                    val mitglied = Mitglied.fromEvents(existingEvents)
                    mitglied.aktualisiereMitglied(properties, sequenceGenerator::nextSequence)
                }
                allNewEvents.addAll(newEvents)
            }
            
            repository.speichereEvents(allNewEvents)
            importRepository.speichereImportEvent(MitgliederImportErfolgreich(sequenznummer = sequenceGenerator.nextSequence()))
        } catch (e: Exception) {
            importRepository.speichereImportEvent(MitgliederImportFehlgeschlagen(e.message ?: "Unbekannter Fehler", sequenznummer = sequenceGenerator.nextSequence()))
            throw e
        }
    }

}
