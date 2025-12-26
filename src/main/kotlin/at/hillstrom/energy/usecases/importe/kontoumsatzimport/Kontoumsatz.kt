package at.hillstrom.energy.usecases.importe.kontoumsatzimport

import at.hillstrom.energy.Buchungsreferenz
import at.hillstrom.energy.KontoumsatzImportiert
import at.hillstrom.energy.KontoumsatzProperties

class Kontoumsatz private constructor(
    val buchungsreferenz: Buchungsreferenz,
    val properties: KontoumsatzProperties
) {

    constructor(event: KontoumsatzImportiert) : this(event.buchungsreferenz, event.properties)

    fun validiereGleichheit(andereProperties: KontoumsatzProperties) {
        if (properties != andereProperties) {
            throw IllegalArgumentException("Umsatz mit Referenz $buchungsreferenz existiert bereits, aber die Werte weichen ab. Vorhanden: $properties, Neu: $andereProperties")
        }
    }

    companion object {
        fun importiereUmsatz(properties: KontoumsatzProperties): KontoumsatzImportiert {
            return KontoumsatzImportiert(properties.buchungsreferenz, properties.copy())
        }
    }
}