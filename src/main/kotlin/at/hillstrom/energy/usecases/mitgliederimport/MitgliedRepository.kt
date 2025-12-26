package at.hillstrom.energy.usecases.mitgliederimport

import at.hillstrom.energy.ImportEvent
import at.hillstrom.energy.MitgliedEvent
import at.hillstrom.energy.Mitgliedsnummer

interface MitgliedRepository {
    fun ladeEvents(kundennummer: Mitgliedsnummer): List<MitgliedEvent>
    fun speichereEvents(events: List<MitgliedEvent>)
    fun speichereImportEvent(event: ImportEvent)
}
