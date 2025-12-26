package at.hillstrom.energy.usecases.importe.mitgliederimport

import at.hillstrom.energy.MitgliedProperties
import at.hillstrom.energy.Mitgliedsnummer

interface MitgliedImportSource : Iterator<Pair<Mitgliedsnummer, MitgliedProperties>>
