package at.hillstrom.energy.usecases.unbezahlterechnungen

import at.hillstrom.energy.Bruttobetrag
import at.hillstrom.energy.Rechnungsdatum
import at.hillstrom.energy.Rechnungsnummer

enum class RechnungsStatus {
    OFFEN,
    BEZAHLT
}

data class UnbezahlteRechnung(
    val rechnungsnummer: Rechnungsnummer,
    val rechnungsdatum: Rechnungsdatum,
    val bruttobetrag: Bruttobetrag,
    val status: RechnungsStatus
)

interface UnbezahlteRechnungenRepository {
    fun save(rechnung: UnbezahlteRechnung)
    fun findByRechnungsnummer(rechnungsnummer: Rechnungsnummer): UnbezahlteRechnung?
    fun findUnbezahlteAb(datum: Rechnungsdatum): List<UnbezahlteRechnung>
    
    fun markiereAlsBezahlt(rechnungsnummer: Rechnungsnummer)
    fun istAlsBezahltMarkiert(rechnungsnummer: Rechnungsnummer): Boolean
}

interface ProcessedEventRepository {
    fun saveLastProcessedSequence(sequence: Long)
    fun getLastProcessedSequence(): Long
}
