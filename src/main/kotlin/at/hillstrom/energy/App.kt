package at.hillstrom.energy

import at.hillstrom.energy.usecases.importe.ImportRepository
import at.hillstrom.energy.usecases.importe.kontoumsatzimport.KontoumsatzImportService
import at.hillstrom.energy.usecases.importe.kontoumsatzimport.KontoumsatzRepository
import at.hillstrom.energy.usecases.importe.mitgliederimport.MitgliederImportService
import at.hillstrom.energy.usecases.importe.mitgliederimport.MitgliedRepository
import at.hillstrom.energy.usecases.importe.rechnungsimport.RechnungRepository
import at.hillstrom.energy.usecases.importe.rechnungsimport.RechnungsImportService
import at.hillstrom.energy.usecases.unbezahlterechnungen.ProcessedEventRepository
import at.hillstrom.energy.usecases.unbezahlterechnungen.UnbezahlteRechnungenRepository
import at.hillstrom.energy.usecases.unbezahlterechnungen.ZahlungsClearingReadModel
import at.hillstrom.energy.usecases.unbezahlterechnungen.ZahlungsMatcher
import at.hillstrom.energy.usecases.unbezahlterechnungen.ZahlungsMatcherService

class App(
    val mitgliedRepository: MitgliedRepository,
    val rechnungRepository: RechnungRepository,
    val kontoumsatzRepository: KontoumsatzRepository,
    val importRepository: ImportRepository,
    val unbezahlteRechnungenRepository: UnbezahlteRechnungenRepository,
    val processedEventRepository: ProcessedEventRepository,
    val sequenceGenerator: EventSequenceGenerator
) {
    val mitgliederImportService = MitgliederImportService(mitgliedRepository, importRepository, sequenceGenerator)
    val rechnungsImportService = RechnungsImportService(rechnungRepository, importRepository, sequenceGenerator)
    val kontoumsatzImportService = KontoumsatzImportService(kontoumsatzRepository, importRepository, sequenceGenerator)

    val zahlungsClearingReadModel = ZahlungsClearingReadModel(unbezahlteRechnungenRepository)
    val zahlungsMatcherService = ZahlungsMatcherService(kontoumsatzRepository, ZahlungsMatcher(sequenceGenerator), zahlungsClearingReadModel, processedEventRepository)
}
