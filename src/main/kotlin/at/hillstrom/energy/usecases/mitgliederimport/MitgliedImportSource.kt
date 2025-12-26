package at.hillstrom.energy.usecases.mitgliederimport

import at.hillstrom.energy.MitgliedProperties
import at.hillstrom.energy.Mitgliedsnummer

interface MitgliedImportSource : Iterator<Pair<Mitgliedsnummer, MitgliedProperties>>
