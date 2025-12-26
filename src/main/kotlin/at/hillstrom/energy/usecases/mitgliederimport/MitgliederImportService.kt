package at.hillstrom.energy.usecases.mitgliederimport

import at.hillstrom.energy.MitgliederImportErfolgreich
import at.hillstrom.energy.MitgliederImportFehlgeschlagen
import at.hillstrom.energy.MitgliedProperties
import at.hillstrom.energy.Mitgliedsnummer

class MitgliederImportService(private val repository: MitgliedRepository) {

    fun importiere(source: MitgliedImportSource) {
        try {
            val allNewEvents = mutableListOf<at.hillstrom.energy.MitgliedEvent>()
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
            repository.speichereImportEvent(MitgliederImportErfolgreich())
        } catch (e: Exception) {
            repository.speichereImportEvent(MitgliederImportFehlgeschlagen(e.message ?: "Unbekannter Fehler"))
            throw e
        }
    }

}
