package at.hillstrom.energy

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

// --- Allgemein ---
data class Betrag(val wert: BigDecimal)
data class Datum(val wert: LocalDate)

// --- Mitglied ---
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

// --- Import Events ---

sealed class ImportEvent

data class MitgliederImportErfolgreich(val zeitpunkt: java.time.Instant = java.time.Instant.now()) : ImportEvent()

data class MitgliederImportFehlgeschlagen(
    val fehler: String,
    val zeitpunkt: java.time.Instant = java.time.Instant.now()
) : ImportEvent()

data class RechnungenImportErfolgreich(val zeitpunkt: java.time.Instant = java.time.Instant.now()) : ImportEvent()

data class RechnungenImportFehlgeschlagen(
    val fehler: String,
    val zeitpunkt: java.time.Instant = java.time.Instant.now()
) : ImportEvent()

data class KontoumsaetzeImportErfolgreich(val zeitpunkt: java.time.Instant = java.time.Instant.now()) : ImportEvent()

data class KontoumsaetzeImportFehlgeschlagen(
    val fehler: String,
    val zeitpunkt: java.time.Instant = java.time.Instant.now()
) : ImportEvent()

// --- Rechnung ---

data class Rechnungsnummer(val wert: String)

data class Nettobetrag(val betrag: Betrag)
data class Bruttobetrag(val betrag: Betrag)
data class Umsatzsteuer(val betrag: Betrag)
data class Rechnungsdatum(val datum: Datum)
data class Faelligkeitsdatum(val datum: Datum)

enum class RechnungsSteuerklasse {
    PRIVAT,
    UMSATZSTEUERPFLICHTIG;

    companion object {
        fun from(steuerklasse: Steuerklasse): RechnungsSteuerklasse {
            return when (steuerklasse) {
                Steuerklasse.PRIVAT -> PRIVAT
                Steuerklasse.KLEINUNTERNEHMER -> PRIVAT
                Steuerklasse.UMSATZSTEUERPFLICHTIG -> UMSATZSTEUERPFLICHTIG
            }
        }
    }
}

data class RechnungProperties(
    val mitgliedsnummer: Mitgliedsnummer,
    val nettobetrag: Nettobetrag,
    val bruttobetrag: Bruttobetrag,
    val umsatzsteuer: Umsatzsteuer,
    val rechnungsnummer: Rechnungsnummer,
    val rechnungsdatum: Rechnungsdatum,
    val faelligkeitsdatum: Faelligkeitsdatum,
    val steuerklasse: RechnungsSteuerklasse
)

sealed class RechnungEvent {
    abstract val id: UUID
}

data class RechnungErstellt(
    override val id: UUID,
    val properties: RechnungProperties
) : RechnungEvent()

// --- Kontoumsatz ---

data class Partnername(val wert: String)
data class IBAN(val wert: String)
data class Buchungsreferenz(val wert: String)
data class Zahlungsreferenz(val wert: String)
data class MandatsId(val wert: String)
data class Buchungsdetails(val wert: String)

data class Buchungsdatum(val wert: Datum)
data class Kontoumsatzbetrag(val wert: Betrag)

data class KontoumsatzProperties(
    val buchungsdatum: Buchungsdatum,
    val partnername: Partnername,
    val partnerIban: IBAN?,
    val betrag: Kontoumsatzbetrag,
    val buchungsdetails: Buchungsdetails?,
    val buchungsreferenz: Buchungsreferenz,
    val zahlungsreferenz: Zahlungsreferenz?,
    val mandatsId: MandatsId?
)

sealed class KontoumsatzEvent {
    abstract val buchungsreferenz: Buchungsreferenz
}

data class KontoumsatzImportiert(
    override val buchungsreferenz: Buchungsreferenz,
    val properties: KontoumsatzProperties
) : KontoumsatzEvent()
