package org.wit.scorewriter.console.models

class CompositionModel(
        var title: String,
        var artist: String = "Unknown",
        var bpm: Int = 120
) {
    val melody: MutableList<Note> = mutableListOf<Note>()

    inner class Note(
        var name: Char,
        var octave: Int,
        var isSharp: Boolean = false,
        var duration: Int = 8
    ){ }

    fun insertNote(note: Note, index: Int = melody.size)
    {
        melody.add(index, note)
    }
}