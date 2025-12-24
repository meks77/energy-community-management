package at.hillstrom.energy.domain.rechnung

import at.hillstrom.energy.domain.mitglied.Mitgliedsnummer
import at.hillstrom.energy.domain.mitglied.Steuerklasse
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class Betrag(val wert: BigDecimal)
data class Rechnungsnummer(val wert: String)
data class Datum(val wert: LocalDate)

data class Nettobetrag(val wert: Betrag)
data class Bruttobetrag(val wert: Betrag)
data class Umsatzsteuer(val wert: Betrag)
data class Rechnungsdatum(val wert: Datum)
data class Faelligkeitsdatum(val wert: Datum)

enum class RechnungsSteuerklasse {
    PRIVAT,
    UMSATZSTEUERPFLICHTIG;

    companion object {
        fun from(steuerklasse: Steuerklasse): RechnungsSteuerklasse {
            return when (steuerklasse) {
                Steuerklasse.PRIVAT -> PRIVAT
                Steuerklasse.KLEINUNTERNEHMER -> PRIVAT
                Steuerklasse.UMSATZSTEUERPFLICHTIG -> UMSATZSTEUERPFLICHTIG
            }
        }
    }
}

data class RechnungProperties(
    val mitgliedsnummer: Mitgliedsnummer,
    val nettobetrag: Nettobetrag,
    val bruttobetrag: Bruttobetrag,
    val umsatzsteuer: Umsatzsteuer,
    val rechnungsnummer: Rechnungsnummer,
    val rechnungsdatum: Rechnungsdatum,
    val faelligkeitsdatum: Faelligkeitsdatum,
    val steuerklasse: RechnungsSteuerklasse
)

sealed class RechnungEvent {
    abstract val id: UUID
}

data class RechnungErstellt(
    override val id: UUID,
    val properties: RechnungProperties
) : RechnungEvent()
