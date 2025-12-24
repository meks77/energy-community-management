package at.hillstrom.energy.domain.mitglied

data class Mitgliedsnummer(val wert: String)

data class Name(val wert: String)
data class Email(val wert: String)

data class Strasse(val wert: String)
data class Hausnummer(val wert: String)
data class PLZ(val wert: String)
data class Ort(val wert: String)

enum class Steuerklasse {
    PRIVAT,
    KLEINUNTERNEHMER,
    UMSATZSTEUERPFLICHTIG
}

data class Adresse(
    val strasse: Strasse,
    val hausnummer: Hausnummer,
    val plz: PLZ,
    val ort: Ort
)

data class MitgliedProperties(
    val name: Name,
    val adresse: Adresse,
    val email: Email,
    val steuerklasse: Steuerklasse
)

sealed class MitgliedEvent {
    abstract val kundennummer: Mitgliedsnummer
}

data class MitgliedAngelegt(
    override val kundennummer: Mitgliedsnummer,
    val name: Name,
    val adresse: Adresse,
    val email: Email,
    val steuerklasse: Steuerklasse
) : MitgliedEvent()

sealed class MitgliedGeandertEvent : MitgliedEvent()

data class NameGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neuerName: Name
) : MitgliedGeandertEvent()

data class AdresseGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neueAdresse: Adresse
) : MitgliedGeandertEvent()

data class EmailGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neueEmail: Email
) : MitgliedGeandertEvent()

data class SteuerklasseGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neueSteuerklasse: Steuerklasse
) : MitgliedGeandertEvent()
