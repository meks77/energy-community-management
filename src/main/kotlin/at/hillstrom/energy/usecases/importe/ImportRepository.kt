package at.hillstrom.energy.usecases.importe

import at.hillstrom.energy.ImportEvent

interface ImportRepository {
    fun speichereImportEvent(event: ImportEvent)
}
