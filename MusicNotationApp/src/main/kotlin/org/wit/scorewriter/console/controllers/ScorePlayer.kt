package org.wit.scorewriter.console.controllers

import org.wit.scorewriter.console.models.CompositionModel
import javax.sound.midi.*

class ScorePlayer {

    val sequencer = MidiSystem.getSequencer()
    var instNo = 1

    fun play(composition: CompositionModel)
    {
        sequencer.open()
        // set parts per quarter note to 4 (1 tick = sixteenth note)
        val sequence = Sequence(Sequence.PPQ, 4)

        val track = sequence.createTrack()

        // set instrument to grand piano
        val msgInst = ShortMessage()
        msgInst.setMessage(192, 1, instNo, 0)
        val eventInst = MidiEvent(msgInst, 1)
        track.add(eventInst)

        // add notes
        var tickPos = 1
        for (note in composition.melody){

            val pitch = noteNameToPitch(note)
            val duration = 16 / note.duration

            // note on
            val noteOn = ShortMessage()
            noteOn.setMessage(144, 1, pitch, 100)
            val eventOn = MidiEvent(noteOn, tickPos.toLong())

            track.add(eventOn)
            tickPos += duration

            // note off
            val noteOff = ShortMessage()
            noteOff.setMessage(128, 1, pitch, 100)
            val eventOff = MidiEvent(noteOff, tickPos.toLong())

            track.add(eventOff)
        }

        sequencer.sequence = sequence
        sequencer.tempoInBPM = composition.bpm.toFloat()
        sequencer.start()
        while (true) {
            if (!sequencer.isRunning){
                return
            }
        }
    }

    private val noteNameValues = mapOf(
        'C' to 0, 'D' to 2, 'E' to 4, 'F' to 5, 'G' to 7, 'A' to 9, 'B' to 11
    )

    private fun noteNameToPitch(note: CompositionModel.Note): Int
    {
        val nameVal = noteNameValues[note.name]!!
        val octaveVal = (note.octave - 4) * 12
        val sharp = if (note.isSharp) 1 else 0

        return 60 + nameVal + octaveVal + sharp
    }
}