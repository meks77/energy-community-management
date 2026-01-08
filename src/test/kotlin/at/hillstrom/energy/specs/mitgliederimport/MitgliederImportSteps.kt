package at.hillstrom.energy.specs.mitgliederimport

import at.hillstrom.energy.*
import at.hillstrom.energy.specs.SharedAppContext
import at.hillstrom.energy.usecases.importe.mitgliederimport.Mitglied
import at.hillstrom.energy.usecases.importe.mitgliederimport.MitgliedImportSource
import io.cucumber.java.de.Angenommen
import io.cucumber.java.de.Dann
import io.cucumber.java.de.Wenn
import kotlin.test.assertEquals

class MitgliederImportSteps(private val context: SharedAppContext) {

    private val membersInSource = mutableListOf<MitgliedData>()

    private val repository get() = context.mitgliedRepository
    private val app get() = context.app

    @Angenommen("folgende Mitglieder wurden bereits importiert:")
    fun seienFolgendeBereitsExistierendeMitglieder(rows: List<Map<String, String>>) {
        rows.forEach { row ->
            val kundennummer = Mitgliedsnummer(row["Kundennummer"]!!)
            val properties = MitgliedProperties(
                Name(row["Name"]!!),
                Adresse(Strasse(""), Hausnummer(""), PLZ(""), Ort("")),
                Email(row["E-Mail"]!!),
                Steuerklasse.PRIVAT
            )
            repository.speichereEvents(listOf(Mitglied.erstelleMitglied(kundennummer, properties, app.sequenceGenerator.nextSequence())))
        }
    }

    @Angenommen("folgende Mitglieder sind in der Import-Quelle:")
    fun seienFolgendeMitgliederInDerImportQuelle(expectedRows: List<Map<String, String>>) {
        expectedRows.forEach { row ->
            membersInSource.add(MitgliedData(
                row["Kundennummer"]!!,
                row["Name"]!!,
                row["E-Mail"]!!
            ))
        }
    }

    @Wenn("der Import ausgeführt wird")
    fun der_import_ausgefuehrt_wird() {
        val source = object : MitgliedImportSource {
            private val iterator = membersInSource.iterator()
            override fun hasNext(): Boolean = iterator.hasNext()
            override fun next(): Pair<Mitgliedsnummer, MitgliedProperties> {
                val data = iterator.next()
                return Mitgliedsnummer(data.kundennummer) to MitgliedProperties(
                    Name(data.name),
                    Adresse(Strasse(""), Hausnummer(""), PLZ(""), Ort("")),
                    Email(data.email),
                    Steuerklasse.PRIVAT
                )
            }
        }
        app.mitgliederImportService.importiere(source)
    }

    @Dann("sind folgende Mitglieder im System vorhanden:")
    fun sind_folgende_mitglieder_im_system_vorhanden(expectedRows: List<Map<String, String>>) {
        val actualMembers = context.getMitgliederEvents().map { (nr, events) ->
            val mitglied = Mitglied.fromEvents(events)
            MitgliedData(nr.wert, mitglied.name.wert, mitglied.email.wert)
        }.sortedBy { it.kundennummer }

        assertEquals(expectedRows.size, actualMembers.size, "Anzahl der Mitglieder stimmt nicht überein")
        
        expectedRows.forEachIndexed { index, expected ->
            val actual = actualMembers[index]
            assertEquals(expected["Kundennummer"], actual.kundennummer)
            assertEquals(expected["Name"], actual.name)
            assertEquals(expected["E-Mail"], actual.email)
        }
    }

    data class MitgliedData(val kundennummer: String, val name: String, val email: String)
}
