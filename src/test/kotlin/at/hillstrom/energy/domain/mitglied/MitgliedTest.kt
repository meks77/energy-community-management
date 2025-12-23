package at.hillstrom.energy.domain.mitglied

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MitgliedTest {

    private val kundennummer = "K12345"
    private val initialAdresse = Adresse("Musterstraße", "1", "1234", "Musterstadt")
    private val initialProperties = MitgliedProperties(
        "Max Mustermann",
        initialAdresse,
        "max@mustermann.de",
        Steuerklasse.PRIVAT
    )

    @Test
    fun `beim ersten Import wird MitgliedAngelegt Event erzeugt`() {
        val event = Mitglied.erstelleMitglied(kundennummer, initialProperties)

        assertEquals(kundennummer, event.kundennummer)
        assertEquals(initialProperties.name, event.name)
        assertEquals(initialProperties.adresse, event.adresse)
        assertEquals(initialProperties.email, event.email)
        assertEquals(initialProperties.steuerklasse, event.steuerklasse)
    }

    @Test
    fun `wenn sich Eigenschaften aendern, werden entsprechende Events erzeugt`() {
        val event = Mitglied.erstelleMitglied(kundennummer, initialProperties)
        val mitglied = Mitglied(event)

        val neueAdresse = initialAdresse.copy(strasse = "Neue Straße")
        val neueProperties = initialProperties.copy(
            name = "Max Musterfrau",
            adresse = neueAdresse,
            email = "max@musterfrau.de",
            steuerklasse = Steuerklasse.UMSATZSTEUERPFLICHTIG
        )

        val events = mitglied.aktualisiereMitglied(neueProperties)

        assertEquals(4, events.size)
        assertTrue(events.any { it is NameGeaendert && it.neuerName == "Max Musterfrau" })
        assertTrue(events.any { it is AdresseGeaendert && it.neueAdresse == neueAdresse })
        assertTrue(events.any { it is EmailGeaendert && it.neueEmail == "max@musterfrau.de" })
        assertTrue(events.any { it is SteuerklasseGeaendert && it.neueSteuerklasse == Steuerklasse.UMSATZSTEUERPFLICHTIG })
    }

    @Test
    fun `wenn sich keine Eigenschaften aendern, werden keine Events erzeugt`() {
        val event = Mitglied.erstelleMitglied(kundennummer, initialProperties)
        val mitglied = Mitglied(event)

        val events = mitglied.aktualisiereMitglied(initialProperties)

        assertEquals(0, events.size)
    }

    @Test
    fun `apply aktualisiert den Namen`() {
        val mitglied = Mitglied(Mitglied.erstelleMitglied(kundennummer, initialProperties))
        val event = NameGeaendert(kundennummer, "Neuer Name")
        
        mitglied.apply(event)
        
        assertEquals("Neuer Name", mitglied.name)
    }

    @Test
    fun `apply aktualisiert die Adresse`() {
        val mitglied = Mitglied(Mitglied.erstelleMitglied(kundennummer, initialProperties))
        val neueAdresse = initialAdresse.copy(strasse = "Neue Strasse")
        val event = AdresseGeaendert(kundennummer, neueAdresse)
        
        mitglied.apply(event)
        
        assertEquals(neueAdresse, mitglied.adresse)
    }

    @Test
    fun `apply aktualisiert die Email`() {
        val mitglied = Mitglied(Mitglied.erstelleMitglied(kundennummer, initialProperties))
        val event = EmailGeaendert(kundennummer, "neu@email.de")
        
        mitglied.apply(event)
        
        assertEquals("neu@email.de", mitglied.email)
    }

    @Test
    fun `apply aktualisiert die Steuerklasse`() {
        val mitglied = Mitglied(Mitglied.erstelleMitglied(kundennummer, initialProperties))
        val event = SteuerklasseGeaendert(kundennummer, Steuerklasse.KLEINUNTERNEHMER)
        
        mitglied.apply(event)
        
        assertEquals(Steuerklasse.KLEINUNTERNEHMER, mitglied.steuerklasse)
    }
}
