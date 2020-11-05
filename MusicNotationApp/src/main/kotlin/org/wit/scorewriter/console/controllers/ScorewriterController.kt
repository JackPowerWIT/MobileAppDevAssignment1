package org.wit.scorewriter.console.controllers

import org.wit.scorewriter.console.views.ScorewriterView
import org.wit.scorewriter.console.models.CompositionModel

class ScorewriterController {

    val compositions: MutableList<CompositionModel> = mutableListOf()

    fun start()
    {
        insertDummyData()
        val view = ScorewriterView()
        view.drawScore(compositions[0])
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