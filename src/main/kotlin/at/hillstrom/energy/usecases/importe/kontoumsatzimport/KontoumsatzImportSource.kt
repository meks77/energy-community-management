package at.hillstrom.energy.usecases.importe.kontoumsatzimport

import at.hillstrom.energy.KontoumsatzProperties

interface KontoumsatzImportSource {
    fun hasNext(): Boolean
    fun next(): KontoumsatzProperties
}
