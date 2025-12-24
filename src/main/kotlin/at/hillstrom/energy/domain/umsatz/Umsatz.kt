package at.hillstrom.energy.domain.umsatz

class Umsatz private constructor(
    val buchungsreferenz: String,
    val properties: UmsatzProperties
) {

    constructor(event: UmsatzImportiert) : this(event.buchungsreferenz, event.properties)

    fun validiereGleichheit(andereProperties: UmsatzProperties) {
        if (properties != andereProperties) {
            throw IllegalArgumentException("Umsatz mit Referenz $buchungsreferenz existiert bereits, aber die Werte weichen ab. Vorhanden: $properties, Neu: $andereProperties")
        }
    }

    companion object {
        fun importiereUmsatz(properties: UmsatzProperties): UmsatzImportiert {
            return UmsatzImportiert(properties.buchungsreferenz, properties.copy())
        }
    }
}
