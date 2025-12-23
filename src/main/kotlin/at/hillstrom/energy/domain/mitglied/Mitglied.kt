package at.hillstrom.energy.domain.mitglied

class Mitglied private constructor(
    val kundennummer: Mitgliedsnummer,
    var name: String,
    var adresse: Adresse,
    var email: String,
    var steuerklasse: Steuerklasse
) {

    constructor(importiertEvent: MitgliedAngelegt) : this(importiertEvent.kundennummer, importiertEvent.name,
        importiertEvent.adresse, importiertEvent.email, importiertEvent.steuerklasse)

    companion object {
        fun erstelleMitglied(kundennummer: Mitgliedsnummer, properties: MitgliedProperties): MitgliedAngelegt {
            return MitgliedAngelegt(kundennummer, properties.name, properties.adresse, properties.email, properties.steuerklasse)
        }
    }


    fun aktualisiereMitglied(neueProperties: MitgliedProperties): List<MitgliedGeandertEvent> {
        val events = mutableListOf<MitgliedGeandertEvent>()

        if (name != neueProperties.name) {
            events.add(NameGeaendert(kundennummer, neueProperties.name))
        }

        if (adresse != neueProperties.adresse) {
            events.add(AdresseGeaendert(kundennummer, neueProperties.adresse))
        }

        if (email != neueProperties.email) {
            events.add(EmailGeaendert(kundennummer, neueProperties.email))
        }

        if (steuerklasse != neueProperties.steuerklasse) {
            events.add(SteuerklasseGeaendert(kundennummer, neueProperties.steuerklasse))
        }

        return events
    }

    fun apply(event: MitgliedGeandertEvent) {
        when (event) {
            is NameGeaendert -> {
                this.name = event.neuerName
            }
            is AdresseGeaendert -> {
                this.adresse = event.neueAdresse
            }
            is EmailGeaendert -> {
                this.email = event.neueEmail
            }
            is SteuerklasseGeaendert -> {
                this.steuerklasse = event.neueSteuerklasse
            }
        }
    }
}
