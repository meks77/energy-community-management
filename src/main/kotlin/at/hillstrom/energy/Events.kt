package at.hillstrom.energy

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

// --- Allgemein ---
interface DomainEvent {
    val sequenznummer: Long
}

interface EventSequenceGenerator {
    fun nextSequence(): Long
}


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

sealed class MitgliedEvent : DomainEvent {
    abstract val kundennummer: Mitgliedsnummer
    abstract override val sequenznummer: Long
}

data class MitgliedAngelegt(
    override val kundennummer: Mitgliedsnummer,
    val name: Name,
    val adresse: Adresse,
    val email: Email,
    val steuerklasse: Steuerklasse,
    override val sequenznummer: Long
) : MitgliedEvent()

sealed class MitgliedGeandertEvent : MitgliedEvent()

data class NameGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neuerName: Name,
    override val sequenznummer: Long
) : MitgliedGeandertEvent()

data class AdresseGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neueAdresse: Adresse,
    override val sequenznummer: Long
) : MitgliedGeandertEvent()

data class EmailGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neueEmail: Email,
    override val sequenznummer: Long
) : MitgliedGeandertEvent()

data class SteuerklasseGeaendert(
    override val kundennummer: Mitgliedsnummer,
    val neueSteuerklasse: Steuerklasse,
    override val sequenznummer: Long
) : MitgliedGeandertEvent()

// --- Import Events ---

sealed class ImportEvent : DomainEvent {
    abstract override val sequenznummer: Long
}

data class MitgliederImportErfolgreich(
    val zeitpunkt: java.time.Instant = java.time.Instant.now(),
    override val sequenznummer: Long
) : ImportEvent()

data class MitgliederImportFehlgeschlagen(
    val fehler: String,
    val zeitpunkt: java.time.Instant = java.time.Instant.now(),
    override val sequenznummer: Long
) : ImportEvent()

data class RechnungenImportErfolgreich(
    val zeitpunkt: java.time.Instant = java.time.Instant.now(),
    override val sequenznummer: Long
) : ImportEvent()

data class RechnungenImportFehlgeschlagen(
    val fehler: String,
    val zeitpunkt: java.time.Instant = java.time.Instant.now(),
    override val sequenznummer: Long
) : ImportEvent()

data class KontoumsaetzeImportErfolgreich(
    val zeitpunkt: java.time.Instant = java.time.Instant.now(),
    override val sequenznummer: Long
) : ImportEvent()

data class KontoumsaetzeImportFehlgeschlagen(
    val fehler: String,
    val zeitpunkt: java.time.Instant = java.time.Instant.now(),
    override val sequenznummer: Long
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

data class RechnungErstellt(
    val id: Rechnungsnummer,
    val properties: RechnungProperties,
    override val sequenznummer: Long
) : DomainEvent

sealed class ZahlungsClearingEvent : DomainEvent {
    abstract val id: UUID
    abstract override val sequenznummer: Long
}

data class RechnungBeglichen(
    override val id: UUID = UUID.randomUUID(),
    val rechnungsnummer: Rechnungsnummer,
    val buchungsreferenz: Buchungsreferenz,
    val beglichenAm: Datum,
    override val sequenznummer: Long
) : ZahlungsClearingEvent()

data class ZahlungNichtZugeordnet(
    override val id: UUID = UUID.randomUUID(),
    val buchungsreferenz: Buchungsreferenz,
    val zeitpunkt: Datum,
    override val sequenznummer: Long
) : ZahlungsClearingEvent()

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

sealed class KontoumsatzEvent : DomainEvent {
    abstract val buchungsreferenz: Buchungsreferenz
    abstract override val sequenznummer: Long
}

data class KontoumsatzImportiert(
    override val buchungsreferenz: Buchungsreferenz,
    val properties: KontoumsatzProperties,
    override val sequenznummer: Long
) : KontoumsatzEvent()
