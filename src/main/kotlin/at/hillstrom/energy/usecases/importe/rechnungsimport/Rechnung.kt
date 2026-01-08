package at.hillstrom.energy.usecases.importe.rechnungsimport

import at.hillstrom.energy.RechnungErstellt
import at.hillstrom.energy.RechnungProperties
import at.hillstrom.energy.Rechnungsnummer

class Rechnung private constructor(
    val id: Rechnungsnummer,
    val properties: RechnungProperties
) {

    constructor(event: RechnungErstellt) : this(event.id, event.properties)

    fun validiereGleichheit(andereProperties: RechnungProperties) {
        if (properties != andereProperties) {
            throw IllegalArgumentException("Rechnung mit ID $id existiert bereits, aber die Werte weichen ab. Vorhanden: $properties, Neu: $andereProperties")
        }
    }

    companion object {
        fun erstelleRechnung(properties: RechnungProperties): RechnungErstellt {
            return RechnungErstellt(properties.rechnungsnummer, properties)
        }
    }
}