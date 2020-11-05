package org.wit.scorewriter.console.controllers

import org.hexworks.zircon.api.component.Container
import org.hexworks.zircon.api.uievent.*
import org.wit.scorewriter.console.views.ScorewriterView
import org.wit.scorewriter.console.models.CompositionModel

class ScorewriterController {

    val compositions: MutableList<CompositionModel> = mutableListOf()

    val view: ScorewriterView = ScorewriterView()

    // the user panel which is active
    var activePanel: Container = view.scorePanel
    // the active elements within each panel
    var activeNoteIndex: Int = 0
    var activeInfoIndex: Int = 0
    var activeCompositionIndex: Int = 0

    fun start()
    {
        addEventHandlers()
        insertDummyData()
        view.drawScore(compositions[0])
    }

    fun addEventHandlers()
    {
        view.tileGrid.processKeyboardEvents(KeyboardEventType.KEY_PRESSED) { event, _ ->
            // panel focus switching
            if (event.ctrlDown){
                activePanel = when(event.code){
                    KeyCode.UP -> view.scorePanel
                    KeyCode.DOWN -> view.controlPanel
                    KeyCode.LEFT -> view.libraryPanel
                    KeyCode.RIGHT -> view.scorePanel
                    else -> activePanel
                }
            }

            when (activePanel){
                view.scorePanel -> {
                    println("moving in score")
                    scorePanelKeyHandler(event)
                }
                view.controlPanel -> {
                    println("moving in controls")
                }
                view.libraryPanel -> {
                    println("moving in library")
                }
            }
        }
    }

    fun scorePanelKeyHandler(event: KeyboardEvent)
    {
        val composition = compositions[activeCompositionIndex]

        when (event.code){
            KeyCode.LEFT -> if (activeNoteIndex > 0) activeNoteIndex--
            KeyCode.RIGHT -> if (activeNoteIndex < composition.melody.size-1) activeNoteIndex++
            KeyCode.UP -> raisePitch(composition.melody[activeNoteIndex])
            KeyCode.DOWN -> lowerPitch(composition.melody[activeNoteIndex])
        }

        view.drawScore(composition, activeNoteIndex)
    }

    fun raisePitch(note: CompositionModel.Note)
    {
        // make a natural note sharp
        if (!note.isSharp){
            note.isSharp = true
        }
        // else if note is lower than a C6 (upper bound)
        else if (note.octave != 6 ){
            // raise the note name
            note.name = if (note.name == 'G') 'A' else note.name+1
            // check if octave changed
            if (note.name == 'C') note.octave++
            // reset sharp
            note.isSharp = false
        }
    }

    fun lowerPitch(note: CompositionModel.Note)
    {
        // make a sharp note natural
        if (note.isSharp){
            note.isSharp = false
        }
        // else if pitch is higher than a C4 (lower bound)
        else if (note.octave > 4 || note.name != 'C'){
            // lower the note name
            note.name = if (note.name == 'A') 'G' else note.name-1
            // check if octave changed
            if (note.name == 'B') note.octave--
            // make note sharp
            note.isSharp = true
        }
    }

    fun controlPanelKeyHandler(event: KeyboardEvent)
    {

    }

    fun libraryPanelKeyHandler(event: KeyboardEvent)
    {

    }

    fun insertDummyData()
    {
        val myComp = CompositionModel("The Lick")
        val notes = listOf("D4", "E4", "F4", "G4" , "E4", "C4", "D4",)
        notes.forEach {
            myComp.insertNote(myComp.Note(it[0], it[1].toString().toInt()))
        }
        compositions.add(myComp)
    }
}