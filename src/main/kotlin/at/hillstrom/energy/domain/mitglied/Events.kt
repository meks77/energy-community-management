package at.hillstrom.energy.domain.mitglied

data class Mitgliedsnummer(val wert: String)

enum class Steuerklasse {
    PRIVAT,
    KLEINUNTERNEHMER,
    UMSATZSTEUERPFLICHTIG
}

data class Adresse(
    val strasse: String,
    val hausnummer: String,
    val plz: String,
    val ort: String
)

data class MitgliedProperties(
    val name: String,
    val adresse: Adresse,
    val email: String,
    val steuerklasse: Steuerklasse
)

sealed class MitgliedEvent {
    abstract val kundennummer: Mitgliedsnummer
}

data class MitgliedAngelegt(
    override val kundennummer: Mitgliedsnummer,
    val name: String,
    val adresse: Adresse,
    val email: String,
    val steuerklasse: Steuerklasse
) : MitgliedEvent()

sealed class MitgliedGeandertEvent : MitgliedEvent()

data class NameGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neuerName: String
) : MitgliedGeandertEvent()

data class AdresseGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neueAdresse: Adresse
) : MitgliedGeandertEvent()

data class EmailGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neueEmail: String
) : MitgliedGeandertEvent()

data class SteuerklasseGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neueSteuerklasse: Steuerklasse
) : MitgliedGeandertEvent()
