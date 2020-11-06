package org.wit.scorewriter.console.controllers

import org.hexworks.zircon.api.component.Container
import org.hexworks.zircon.api.uievent.*
import org.wit.scorewriter.console.views.ScorewriterView
import org.wit.scorewriter.console.models.CompositionModel
import org.wit.scorewriter.console.models.CompositionSerialiser
import java.lang.Exception

class ScorewriterController {

    val compositions: MutableList<CompositionModel> = mutableListOf()

    val view: ScorewriterView = ScorewriterView()

    // the user panel which is active
    var activePanel: Container = view.scorePanel
    // the active elements within each panel
    var activeNoteIndex: Int = 0
    var activeInputIndex: Int = -1
    var activeLibraryIndex: Int = 0

    val serialiser = CompositionSerialiser()

    fun start()
    {
        addEventHandlers()
        //insertDummyData()
        compositions.addAll(serialiser.deserialise())
        view.drawLibrary(compositions, activeLibraryIndex)
        view.drawScore(compositions[activeLibraryIndex])
        view.populateInputs(compositions[activeLibraryIndex])
    }

    fun addEventHandlers()
    {
        view.tileGrid.processKeyboardEvents(KeyboardEventType.KEY_PRESSED) { event, _ ->
            // panel focus switching
            if (event.ctrlDown){
                activePanel = when(event.code){
                    KeyCode.UP -> {
                        view.clearInputFocus()
                        view.clearUserMessage()
                        view.scorePanel
                    }
                    KeyCode.DOWN -> {
                        activeInputIndex = -1   // always start on first field
                        activeNoteIndex = -1
                        view.controlPanel
                    }
                    KeyCode.LEFT -> {
                        view.clearInputFocus()
                        activeNoteIndex = -1
                        view.libraryPanel
                    }
                    KeyCode.RIGHT -> {
                        view.clearInputFocus()
                        view.clearUserMessage()
                        view.scorePanel
                    }
                    else -> activePanel
                }
            }

            when (activePanel){
                view.scorePanel -> {
                    scorePanelKeyHandler(event)
                }
                view.controlPanel -> {
                    controlPanelKeyHandler(event)
                }
                view.libraryPanel -> {
                    libraryPanelKeyHandler(event)
                }
            }
        }
    }

    fun scorePanelKeyHandler(event: KeyboardEvent)
    {
        val composition = compositions[activeLibraryIndex]
        val melody = composition.melody

        when (event.code){
            KeyCode.LEFT -> if (activeNoteIndex > 0) activeNoteIndex--
            KeyCode.RIGHT -> if (activeNoteIndex < melody.size-1) activeNoteIndex++
            KeyCode.UP -> raisePitch(melody[activeNoteIndex])
            KeyCode.DOWN -> lowerPitch(melody[activeNoteIndex])
            KeyCode.KEY_A -> melody.add(activeNoteIndex + 1, composition.Note())
            KeyCode.KEY_X -> if (melody.size > 1) melody.removeAt(activeNoteIndex)
            KeyCode.DIGIT_4 -> melody[activeNoteIndex].duration = 4
            KeyCode.DIGIT_8 -> melody[activeNoteIndex].duration = 8
            KeyCode.DIGIT_6 -> melody[activeNoteIndex].duration = 16
            else -> return
        }

        view.drawScore(composition, activeNoteIndex)
    }

    fun controlPanelKeyHandler(event: KeyboardEvent)
    {
        when (event.code){
            KeyCode.UP -> if (activeInputIndex > 0) activeInputIndex--
            KeyCode.DOWN -> if (activeInputIndex < 2) activeInputIndex++
            else -> return
        }

        view.focusInput(activeInputIndex)
    }

    fun libraryPanelKeyHandler(event: KeyboardEvent)
    {
        when (event.code){
            KeyCode.UP -> if (activeLibraryIndex > 0){
                activeLibraryIndex--
            }
            KeyCode.DOWN -> if (activeLibraryIndex < compositions.size-1){
                activeLibraryIndex++
            }
            KeyCode.KEY_S -> saveData()
            KeyCode.KEY_N -> newScore()
            KeyCode.KEY_X -> deleteScore()
            else -> return
        }

        view.drawLibrary(compositions, activeLibraryIndex)
        view.drawScore(compositions[activeLibraryIndex], activeNoteIndex)
        view.populateInputs(compositions[activeLibraryIndex])
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

    fun newScore()
    {
        if (compositions.size < 8) {
            val newComp = CompositionModel("New Score")
            newComp.insertNote(newComp.Note('B', 4))
            compositions.add(newComp)
            activeLibraryIndex = compositions.size - 1
        }
    }

    fun deleteScore()
    {
        if (compositions.size > 1){
            compositions.removeAt(activeLibraryIndex)
            if (activeLibraryIndex == compositions.size){
                activeLibraryIndex--
            }
        }
    }

    fun saveData()
    {
        val composition = compositions[activeLibraryIndex]
        try {
            val bpmValue = view.bpmInput.text.toInt()
            if (bpmValue > 0) {
                composition.bpm = bpmValue
            }
            else {
                throw Exception()
            }
        }
        catch (e: Exception){
            view.writeMessage(
                    // newline escape doesn't work for some reason
                    "BPM must be a     " +
                    "positive number.")
            return
        }
        val titleString = view.titleInput.text
        if (titleString.isNotEmpty()){
            composition.title = titleString
        }
        else {
            view.writeMessage(
                    "Title cannot be   " +
                    "empty.")
            return
        }
        val artistString = view.artistInput.text
        if (artistString.isNotEmpty()){
            composition.artist = artistString
        }
        else {
            view.writeMessage(
                "Artist cannot be  " +
                     "empty.")
            return
        }
        val result = if (serialiser.serialise(compositions)){
            "Data saved."
        }
        else {
            "Error saving data."
        }
        view.writeMessage(result)
    }

    /*
    fun insertDummyData()
    {
        val myComp = CompositionModel("The Lick")
        var notes = listOf("D4", "E4", "F4", "G4" , "E4", "C4", "D4",)
        notes.forEach {
            myComp.insertNote(myComp.Note(it[0], it[1].toString().toInt()))
        }
        compositions.add(myComp)

        val myComp2 = CompositionModel("Giant Steps")
        notes = listOf("F5", "D5", "B4", "G4", "B5")
        notes.forEach {
            myComp2.insertNote(myComp.Note(it[0], it[1].toString().toInt()))
        }
        compositions.add(myComp2)

        val myComp3 = CompositionModel("A-Train")
        notes = listOf("G4", "E5", "G4", "C5" , "E5", "G4")
        notes.forEach {
            myComp3.insertNote(myComp.Note(it[0], it[1].toString().toInt()))
        }
        compositions.add(myComp3)
    }
     */
}