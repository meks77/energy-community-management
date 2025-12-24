package at.hillstrom.energy.domain.rechnung
import at.hillstrom.energy.*

import java.nio.charset.StandardCharsets
import java.util.UUID

class Rechnung private constructor(
    val id: UUID,
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
            val id = generiereId(properties)
            return RechnungErstellt(id, properties)
        }

        private fun generiereId(properties: RechnungProperties): UUID {
            // Logisch ist die Rechnung aufgrund der Mitgliedsnummer, dem Rechnungsdatum und dem Rechnungsbetrag identifizierbar
            val identifier = "${properties.mitgliedsnummer.wert}|${properties.rechnungsdatum.wert.wert}|${properties.bruttobetrag.wert.wert}"
            return UUID.nameUUIDFromBytes(identifier.toByteArray(StandardCharsets.UTF_8))
        }
    }
}
