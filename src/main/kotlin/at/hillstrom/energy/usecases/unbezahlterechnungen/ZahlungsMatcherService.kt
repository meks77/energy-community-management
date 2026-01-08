package at.hillstrom.energy.usecases.unbezahlterechnungen

import at.hillstrom.energy.KontoumsatzImportiert
import at.hillstrom.energy.RechnungBeglichen
import at.hillstrom.energy.ZahlungsClearingEvent
import at.hillstrom.energy.usecases.importe.kontoumsatzimport.KontoumsatzRepository

class ZahlungsMatcherService(
    private val kontoumsatzRepository: KontoumsatzRepository,
    private val zahlungsMatcher: ZahlungsMatcher,
    private val zahlungsClearingReadModel: ZahlungsClearingReadModel,
    private val processedEventRepository: ProcessedEventRepository
) {

    fun verarbeiteNeueEvents(): List<ZahlungsClearingEvent> {
        val lastSequence = processedEventRepository.getLastProcessedSequence()
        val neueEvents = kontoumsatzRepository.ladeAbSequenz(lastSequence)
            .filterIsInstance<KontoumsatzImportiert>()

        val ergebnisse = mutableListOf<ZahlungsClearingEvent>()
        var currentLastSequence = lastSequence

        for (event in neueEvents) {
            val result = zahlungsMatcher.handle(event)
            if (result is RechnungBeglichen) {
                zahlungsClearingReadModel.handle(result)
            }
            ergebnisse.add(result)
            if (event.sequenznummer > currentLastSequence) {
                currentLastSequence = event.sequenznummer
            }
        }

        if (currentLastSequence > lastSequence) {
            processedEventRepository.saveLastProcessedSequence(currentLastSequence)
        }

        return ergebnisse
    }
}
