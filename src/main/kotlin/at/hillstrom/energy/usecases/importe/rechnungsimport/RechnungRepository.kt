package at.hillstrom.energy.usecases.importe.rechnungsimport

import at.hillstrom.energy.ImportEvent
import at.hillstrom.energy.RechnungEvent
import java.util.UUID

interface RechnungRepository {
    fun ladeEvents(id: UUID): List<RechnungEvent>
    fun speichereEvents(events: List<RechnungEvent>)
}
