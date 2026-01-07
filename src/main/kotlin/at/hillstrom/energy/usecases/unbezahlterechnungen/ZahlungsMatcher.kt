package at.hillstrom.energy.usecases.unbezahlterechnungen

import at.hillstrom.energy.*

class ZahlungsMatcher {

    fun handle(event: KontoumsatzImportiert): ZahlungsClearingEvent {
        val rechnungsnummer = rechnungsnummerDerZahlung(event)
        return if (rechnungsnummer != null) {
            RechnungBeglichen(
                rechnungsnummer = rechnungsnummer,
                buchungsreferenz = event.buchungsreferenz,
                beglichenAm = event.properties.buchungsdatum.wert
            )
        } else {
            ZahlungNichtZugeordnet(
                buchungsreferenz = event.buchungsreferenz,
                zeitpunkt = event.properties.buchungsdatum.wert
            )
        }
    }

    private fun rechnungsnummerDerZahlung(event: KontoumsatzImportiert) : Rechnungsnummer? {
        if (event.properties.zahlungsreferenz?.wert != null && event.properties.zahlungsreferenz.wert.isNotEmpty())
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
}
