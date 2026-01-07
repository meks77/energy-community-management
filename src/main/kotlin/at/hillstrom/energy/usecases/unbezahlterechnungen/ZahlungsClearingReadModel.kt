package at.hillstrom.energy.usecases.unbezahlterechnungen

import at.hillstrom.energy.RechnungBeglichen
import at.hillstrom.energy.RechnungErstellt
import at.hillstrom.energy.Rechnungsdatum

class ZahlungsClearingReadModel(
    private val repository: UnbezahlteRechnungenRepository
) {

    fun handle(event: RechnungErstellt) {
        val rechnungsnummer = event.properties.rechnungsnummer
        val status = if (repository.istAlsBezahltMarkiert(rechnungsnummer)) {
            RechnungsStatus.BEZAHLT
        } else {
            RechnungsStatus.OFFEN
        }

        val rechnung = UnbezahlteRechnung(
            rechnungsnummer = rechnungsnummer,
            rechnungsdatum = event.properties.rechnungsdatum,
            bruttobetrag = event.properties.bruttobetrag,
            status = status
        )
        repository.save(rechnung)
    }

    fun handle(event: RechnungBeglichen) {
        val rechnungsnummer = event.rechnungsnummer

        val rechnung = repository.findByRechnungsnummer(rechnungsnummer)
        if (rechnung != null) {
            repository.save(rechnung.copy(status = RechnungsStatus.BEZAHLT))
        } else {
            // Zahlung vor Rechnung
            repository.markiereAlsBezahlt(rechnungsnummer)
        }
    }

    fun getUnbezahlteRechnungen(abDatum: Rechnungsdatum): List<UnbezahlteRechnung> {
        return repository.findUnbezahlteAb(abDatum).filter { it.status == RechnungsStatus.OFFEN }
    }
}
