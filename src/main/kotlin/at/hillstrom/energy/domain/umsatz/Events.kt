package at.hillstrom.energy.domain.umsatz

import java.math.BigDecimal
import java.time.LocalDate

data class UmsatzProperties(
    val buchungsdatum: LocalDate,
    val partnername: String,
    val partnerIban: String?,
    val betrag: BigDecimal,
    val buchungsdetails: String?,
    val buchungsreferenz: String,
    val zahlungsreferenz: String?,
    val mandatsId: String?
)

sealed class UmsatzEvent {
    abstract val buchungsreferenz: String
}

data class UmsatzImportiert(
    override val buchungsreferenz: String,
    val properties: UmsatzProperties
) : UmsatzEvent()
