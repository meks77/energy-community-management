package at.hillstrom.energy.domain.rechnung
import at.hillstrom.energy.*
import at.hillstrom.energy.usecases.importe.rechnungsimport.Rechnung
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class RechnungTest {

    private var seq = 0L
    private fun nextSeq() = ++seq

    @Test
    fun `erstellt eine Rechnung mit deterministischer ID`() {
        val properties = RechnungProperties(
            mitgliedsnummer = Mitgliedsnummer("M123"),
            nettobetrag = Nettobetrag(Betrag(BigDecimal("100.00"))),
            bruttobetrag = Bruttobetrag(Betrag(BigDecimal("120.00"))),
            umsatzsteuer = Umsatzsteuer(Betrag(BigDecimal("20.00"))),
            rechnungsnummer = Rechnungsnummer("R-2023-001"),
            rechnungsdatum = Rechnungsdatum(Datum(LocalDate.of(2023, 12, 23))),
            faelligkeitsdatum = Faelligkeitsdatum(Datum(LocalDate.of(2024, 1, 23))),
            steuerklasse = RechnungsSteuerklasse.UMSATZSTEUERPFLICHTIG
        )

        val event = Rechnung.erstelleRechnung(properties, nextSeq())
        val rechnung = Rechnung(event)

        assertEquals(properties, rechnung.properties)
        assertNotNull(rechnung.id)

        // Verifiziere Deterministik
        val event2 = Rechnung.erstelleRechnung(properties, nextSeq())
        assertEquals(event.id, event2.id)

        // Verifiziere gleiche ID bei anderem Betrag (da Rechnungsnummer gleich bleibt)
        val properties2 = properties.copy(bruttobetrag = Bruttobetrag(Betrag(BigDecimal("121.00"))))
        val event3 = Rechnung.erstelleRechnung(properties2, nextSeq())
        assertEquals(event.id, event3.id)

        // Verifiziere gleiche ID bei anderem Datum (da Rechnungsnummer gleich bleibt)
        val properties3 = properties.copy(rechnungsdatum = Rechnungsdatum(Datum(LocalDate.of(2023, 12, 24))))
        val event4 = Rechnung.erstelleRechnung(properties3, nextSeq())
        assertEquals(event.id, event4.id)

        // Verifiziere gleiche ID bei anderer Mitgliedsnummer (da Rechnungsnummer gleich bleibt)
        val properties4 = properties.copy(mitgliedsnummer = Mitgliedsnummer("M124"))
        val event5 = Rechnung.erstelleRechnung(properties4, nextSeq())
        assertEquals(event.id, event5.id)

        // Verifiziere unterschiedliche ID bei anderer Rechnungsnummer
        val properties5 = properties.copy(rechnungsnummer = Rechnungsnummer("R-2023-002"))
        val event6 = Rechnung.erstelleRechnung(properties5, nextSeq())
        assertNotEquals(event.id, event6.id)
    }

    @Test
    fun `erkennt wenn eine Rechnung bereits existiert anhand der deterministischen ID`() {
        val properties = RechnungProperties(
            mitgliedsnummer = Mitgliedsnummer("M123"),
            nettobetrag = Nettobetrag(Betrag(BigDecimal("100.00"))),
            bruttobetrag = Bruttobetrag(Betrag(BigDecimal("120.00"))),
            umsatzsteuer = Umsatzsteuer(Betrag(BigDecimal("20.00"))),
            rechnungsnummer = Rechnungsnummer("R-2023-001"),
            rechnungsdatum = Rechnungsdatum(Datum(LocalDate.of(2023, 12, 23))),
            faelligkeitsdatum = Faelligkeitsdatum(Datum(LocalDate.of(2024, 1, 23))),
            steuerklasse = RechnungsSteuerklasse.UMSATZSTEUERPFLICHTIG
        )

        val event = Rechnung.erstelleRechnung(properties, nextSeq())
        val rechnung = Rechnung(event)
        
        // Simuliere einen Store/Repository mit existierenden IDs
        val existierendeRechnungen = mapOf(rechnung.id to rechnung)

        // Neue Erstellung führt zur gleichen ID
        val neuesEvent = Rechnung.erstelleRechnung(properties, nextSeq())
        
        assertTrue(existierendeRechnungen.containsKey(neuesEvent.id), "Die ID sollte bereits im Set der existierenden IDs vorhanden sein")
    }

    @Test
    fun `wirft Fehler wenn Rechnung mit gleicher ID aber abweichenden Werten erstellt wird`() {
        val properties = RechnungProperties(
            mitgliedsnummer = Mitgliedsnummer("M123"),
            nettobetrag = Nettobetrag(Betrag(BigDecimal("100.00"))),
            bruttobetrag = Bruttobetrag(Betrag(BigDecimal("120.00"))),
            umsatzsteuer = Umsatzsteuer(Betrag(BigDecimal("20.00"))),
            rechnungsnummer = Rechnungsnummer("R-2023-001"),
            rechnungsdatum = Rechnungsdatum(Datum(LocalDate.of(2023, 12, 23))),
            faelligkeitsdatum = Faelligkeitsdatum(Datum(LocalDate.of(2024, 1, 23))),
            steuerklasse = RechnungsSteuerklasse.UMSATZSTEUERPFLICHTIG
        )

        val event = Rechnung.erstelleRechnung(properties, nextSeq())
        val rechnung = Rechnung(event)

        assertExceptionBeiValidiereGleichheit(rechnung, properties.copy(mitgliedsnummer = Mitgliedsnummer("M124")))
        assertExceptionBeiValidiereGleichheit(rechnung, properties.copy(nettobetrag = Nettobetrag(Betrag(BigDecimal("101.0")))))
        assertExceptionBeiValidiereGleichheit(rechnung, properties.copy(bruttobetrag = Bruttobetrag(Betrag(BigDecimal("121.0")))))
        assertExceptionBeiValidiereGleichheit(rechnung, properties.copy(umsatzsteuer = Umsatzsteuer(Betrag(BigDecimal("21.0")))))
        assertExceptionBeiValidiereGleichheit(rechnung, properties.copy(rechnungsdatum = Rechnungsdatum(Datum(LocalDate.of(2023, 12, 22)))))
        assertExceptionBeiValidiereGleichheit(rechnung, properties.copy(faelligkeitsdatum = Faelligkeitsdatum(Datum(LocalDate.of(2024, 1, 24)))))
        assertExceptionBeiValidiereGleichheit(rechnung, properties.copy(steuerklasse = RechnungsSteuerklasse.PRIVAT))
    }

    private fun assertExceptionBeiValidiereGleichheit(rechnung: Rechnung,
                          abweichendeProperties: RechnungProperties) {
        assertThrows(IllegalArgumentException::class.java) {
            rechnung.validiereGleichheit(abweichendeProperties)
        }
    }

    @Test
    fun `RechnungsSteuerklasse kann von MitgliedSteuerklasse konvertiert werden`() {
        assertEquals(RechnungsSteuerklasse.PRIVAT, RechnungsSteuerklasse.from(Steuerklasse.PRIVAT))
        assertEquals(RechnungsSteuerklasse.PRIVAT, RechnungsSteuerklasse.from(Steuerklasse.KLEINUNTERNEHMER))
        assertEquals(RechnungsSteuerklasse.UMSATZSTEUERPFLICHTIG, RechnungsSteuerklasse.from(Steuerklasse.UMSATZSTEUERPFLICHTIG))
    }
}
