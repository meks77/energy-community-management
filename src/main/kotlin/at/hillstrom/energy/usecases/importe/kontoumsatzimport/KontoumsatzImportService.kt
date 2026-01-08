package at.hillstrom.energy.usecases.importe.kontoumsatzimport

import at.hillstrom.energy.KontoumsatzEvent
import at.hillstrom.energy.KontoumsatzImportiert
import at.hillstrom.energy.KontoumsaetzeImportErfolgreich
import at.hillstrom.energy.KontoumsaetzeImportFehlgeschlagen
import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.EventSequenceGenerator

class KontoumsatzImportService(
    private val repository: KontoumsatzRepository,
    private val importRepository: ImportRepository,
    private val sequenceGenerator: EventSequenceGenerator
) {

    fun importiere(source: KontoumsatzImportSource) {
        try {
            val allNewEvents = mutableListOf<KontoumsatzEvent>()
            while (source.hasNext()) {
                val properties = source.next()
                val existingEvents = repository.ladeEvents(properties.buchungsreferenz)

                if (existingEvents.isEmpty()) {
                    allNewEvents.add(Kontoumsatz.importiereUmsatz(properties, sequenceGenerator.nextSequence()))
                } else {
                    val umsatz = Kontoumsatz(existingEvents.filterIsInstance<KontoumsatzImportiert>().first())
                    umsatz.validiereGleichheit(properties)
                }
            }

            repository.speichereEvents(allNewEvents)
            importRepository.speichereImportEvent(KontoumsaetzeImportErfolgreich(sequenznummer = sequenceGenerator.nextSequence()))
        } catch (e: Exception) {
            importRepository.speichereImportEvent(KontoumsaetzeImportFehlgeschlagen(e.message ?: "Unbekannter Fehler", sequenznummer = sequenceGenerator.nextSequence()))
            throw e
        }
    }
}
