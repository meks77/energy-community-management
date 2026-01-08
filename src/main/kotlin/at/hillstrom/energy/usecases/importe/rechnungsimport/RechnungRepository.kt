package at.hillstrom.energy.usecases.importe.rechnungsimport

import at.hillstrom.energy.RechnungErstellt
import at.hillstrom.energy.Rechnungsnummer

interface RechnungRepository {
    fun finde(id: Rechnungsnummer): RechnungErstellt?
    fun speichereEvents(events: List<RechnungErstellt>)
}
