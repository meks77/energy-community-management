package at.hillstrom.energy.domain.mitglied

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
    abstract val kundennummer: String
}

data class MitgliedAngelegt(
    override val kundennummer: String,
    val name: String,
    val adresse: Adresse,
    val email: String,
    val steuerklasse: Steuerklasse
) : MitgliedEvent()

sealed class MitgliedGeandertEvent : MitgliedEvent()

data class NameGeaendert(
    override val kundennummer: String,
    val neuerName: String
) : MitgliedGeandertEvent()

data class AdresseGeaendert(
    override val kundennummer: String,
    val neueAdresse: Adresse
) : MitgliedGeandertEvent()

data class EmailGeaendert(
    override val kundennummer: String,
    val neueEmail: String
) : MitgliedGeandertEvent()

data class SteuerklasseGeaendert(
    override val kundennummer: String,
    val neueSteuerklasse: Steuerklasse
) : MitgliedGeandertEvent()
