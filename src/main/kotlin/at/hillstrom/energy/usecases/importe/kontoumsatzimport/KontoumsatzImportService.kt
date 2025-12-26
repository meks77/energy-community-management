package at.hillstrom.energy.usecases.importe.kontoumsatzimport

import at.hillstrom.energy.KontoumsatzEvent
import at.hillstrom.energy.KontoumsatzImportiert
import at.hillstrom.energy.KontoumsaetzeImportErfolgreich
import at.hillstrom.energy.KontoumsaetzeImportFehlgeschlagen
import at.hillstrom.energy.usecases.importe.ImportRepository

class KontoumsatzImportService(
    private val repository: KontoumsatzRepository,
    private val importRepository: ImportRepository
) {

    fun importiere(source: KontoumsatzImportSource) {
        try {
            val allNewEvents = mutableListOf<KontoumsatzEvent>()
            while (source.hasNext()) {
                val properties = source.next()
                val existingEvents = repository.ladeEvents(properties.buchungsreferenz)

                if (existingEvents.isEmpty()) {
                    allNewEvents.add(Kontoumsatz.importiereUmsatz(properties))
                } else {
                    val umsatz = Kontoumsatz(existingEvents.filterIsInstance<KontoumsatzImportiert>().first())
                    umsatz.validiereGleichheit(properties)
                }
            }

            repository.speichereEvents(allNewEvents)
            importRepository.speichereImportEvent(KontoumsaetzeImportErfolgreich())
        } catch (e: Exception) {
            importRepository.speichereImportEvent(KontoumsaetzeImportFehlgeschlagen(e.message ?: "Unbekannter Fehler"))
            throw e
        }
    }
}
