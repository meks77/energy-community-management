package at.hillstrom.energy.domain.rechnung

import at.hillstrom.energy.domain.mitglied.Mitgliedsnummer
import at.hillstrom.energy.domain.mitglied.Steuerklasse
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

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
    val nettobetrag: BigDecimal,
    val bruttobetrag: BigDecimal,
    val umsatzsteuer: BigDecimal,
    val rechnungsnummer: String,
    val rechnungsdatum: LocalDate,
    val faelligkeitsdatum: LocalDate,
    val steuerklasse: RechnungsSteuerklasse
)

sealed class RechnungEvent {
    abstract val id: UUID
}

data class RechnungErstellt(
    override val id: UUID,
    val properties: RechnungProperties
) : RechnungEvent()
