package at.hillstrom.energy.usecases.unbezahlterechnungen

import at.hillstrom.energy.KontoumsatzImportiert
import at.hillstrom.energy.RechnungErstellt
import at.hillstrom.energy.Rechnungsdatum
import at.hillstrom.energy.Rechnungsnummer

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

    fun handle(event: KontoumsatzImportiert) {
        val rechnungsnummer = rechnungsnummerDerZahlung(event)

        if (rechnungsnummer != null) {
            val rechnung = repository.findByRechnungsnummer(rechnungsnummer)
            if (rechnung != null) {
                repository.save(rechnung.copy(status = RechnungsStatus.BEZAHLT))
            } else {
                // Zahlung vor Rechnung
                repository.markiereAlsBezahlt(rechnungsnummer)
            }
        }
    }

    fun rechnungsnummerDerZahlung(event: KontoumsatzImportiert) : Rechnungsnummer? {
        if (event.properties.zahlungsreferenz?.wert != null && !event.properties.zahlungsreferenz.wert.isEmpty())
            return Rechnungsnummer("RN_" + event.properties.zahlungsreferenz.wert)

        if (event.properties.buchungsdetails?.wert != null) {
            val rechnungsnummerRegex = Regex("""RN_\d+""")
            val match = rechnungsnummerRegex.find(event.properties.buchungsdetails.wert)

            if (match != null) {
                return Rechnungsnummer(match.value)
            }
        }

        return null
    }

    fun getUnbezahlteRechnungen(abDatum: Rechnungsdatum): List<UnbezahlteRechnung> {
        return repository.findUnbezahlteAb(abDatum).filter { it.status == RechnungsStatus.OFFEN }
    }
}
