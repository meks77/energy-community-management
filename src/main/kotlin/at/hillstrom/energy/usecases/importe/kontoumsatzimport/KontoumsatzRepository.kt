package at.hillstrom.energy.usecases.importe.kontoumsatzimport

import at.hillstrom.energy.KontoumsatzEvent
import at.hillstrom.energy.Buchungsreferenz

interface KontoumsatzRepository {
    fun speichereEvents(events: List<KontoumsatzEvent>)
    fun ladeEvents(buchungsreferenz: Buchungsreferenz): List<KontoumsatzEvent>
}
