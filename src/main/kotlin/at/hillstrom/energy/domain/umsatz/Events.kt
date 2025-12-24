package at.hillstrom.energy.domain.umsatz

import at.hillstrom.energy.domain.rechnung.Betrag
import at.hillstrom.energy.domain.rechnung.Datum
import java.math.BigDecimal
import java.time.LocalDate

data class Partnername(val wert: String)
data class IBAN(val wert: String)
data class Buchungsreferenz(val wert: String)
data class Zahlungsreferenz(val wert: String)
data class MandatsId(val wert: String)
data class Buchungsdetails(val wert: String)

data class Buchungsdatum(val wert: Datum)
data class Umsatzbetrag(val wert: Betrag)

data class UmsatzProperties(
    val buchungsdatum: Buchungsdatum,
    val partnername: Partnername,
    val partnerIban: IBAN?,
    val betrag: Umsatzbetrag,
    val buchungsdetails: Buchungsdetails?,
    val buchungsreferenz: Buchungsreferenz,
    val zahlungsreferenz: Zahlungsreferenz?,
    val mandatsId: MandatsId?
)

sealed class UmsatzEvent {
    abstract val buchungsreferenz: Buchungsreferenz
}

data class UmsatzImportiert(
    override val buchungsreferenz: Buchungsreferenz,
    val properties: UmsatzProperties
) : UmsatzEvent()
