package at.hillstrom.energy.usecases.mitgliederimport

import at.hillstrom.energy.Adresse
import at.hillstrom.energy.AdresseGeaendert
import at.hillstrom.energy.Email
import at.hillstrom.energy.EmailGeaendert
import at.hillstrom.energy.MitgliedAngelegt
import at.hillstrom.energy.MitgliedGeandertEvent
import at.hillstrom.energy.MitgliedProperties
import at.hillstrom.energy.Mitgliedsnummer
import at.hillstrom.energy.Name
import at.hillstrom.energy.NameGeaendert
import at.hillstrom.energy.Steuerklasse
import at.hillstrom.energy.SteuerklasseGeaendert

class Mitglied private constructor(
    val kundennummer: Mitgliedsnummer,
    var name: Name,
    var adresse: Adresse,
    var email: Email,
    var steuerklasse: Steuerklasse
) {

    constructor(importiertEvent: MitgliedAngelegt) : this(importiertEvent.kundennummer, importiertEvent.name,
        importiertEvent.adresse, importiertEvent.email, importiertEvent.steuerklasse)

    companion object {
        fun erstelleMitglied(kundennummer: Mitgliedsnummer, properties: MitgliedProperties): MitgliedAngelegt {
            return MitgliedAngelegt(kundennummer, properties.name, properties.adresse, properties.email,
                properties.steuerklasse)
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