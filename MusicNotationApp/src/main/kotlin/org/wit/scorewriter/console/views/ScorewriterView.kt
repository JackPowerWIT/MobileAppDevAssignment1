package org.wit.scorewriter.console.views

import org.hexworks.zircon.api.*
import org.hexworks.zircon.api.ComponentDecorations.box
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.color.ANSITileColor
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.api.component.*
import org.hexworks.zircon.api.component.data.ComponentState
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.extensions.toScreen
import org.hexworks.zircon.api.graphics.*
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.api.screen.Screen
import org.hexworks.zircon.api.uievent.ComponentEventType
import org.hexworks.zircon.api.uievent.KeyboardEventType
import org.hexworks.zircon.api.uievent.PreventDefault
import org.hexworks.zircon.api.uievent.StopPropagation
import org.wit.scorewriter.console.models.CompositionModel

class ScorewriterView {

    val tileGrid: TileGrid
    val scorePanel: Panel
    val controlPanel: VBox
    val libraryPanel: VBox

    private val screen: Screen
    private val ledgerLayer: Layer
    private val noteLayer: Layer
    private val scoreCursorLayer: Layer

    private val STAFF_LENGTH: Int

    private val noteStaffMap = mapOf<Char, Int>(
            'B' to 1, 'A' to 2, 'G' to 3, 'F' to 4, 'E' to 5, 'D' to 6, 'C' to 7
    )

    init {
        tileGrid = SwingApplications.startTileGrid(
                AppConfig.newBuilder()
                        .withSize(76, 40)
                        .withDefaultTileset(CP437TilesetResources.rexPaint16x16())
                        .build())

        screen = tileGrid.toScreen()
        screen.theme = ColorThemes.amigaOs()

        // root
        val root: Container = Components.hbox()
            .withSize(screen.size.minus(Size.create(2,2)))
            .withPosition(Position.offset1x1())
            .build()

        screen.addComponent(root)

        // library
        libraryPanel = Components.vbox()
                .withSize(20, root.contentSize.height)
                .withDecorations(box(BoxType.TOP_BOTTOM_DOUBLE, "Library"))
            .build()

        root.addComponent(libraryPanel)

        // editor
        val scoreContainer = Components.vbox()
                .withSize(54, root.contentSize.height)
                .build()

        root.addComponent(scoreContainer)

        // score
        scorePanel = Components.panel()
                .withSize(scoreContainer.width, 30)
                .withDecorations(box(BoxType.TOP_BOTTOM_DOUBLE, "Score"))
                .build()

        scoreContainer.addComponent(scorePanel)
        STAFF_LENGTH = scorePanel.width-6
        drawStaff()

        // ledger line layer
        ledgerLayer = Layer.newBuilder()
                .withTileGraphics(DrawSurfaces.tileGraphicsBuilder()
                        .withSize(STAFF_LENGTH,15)
                        .build())
                .withOffset(scorePanel.absolutePosition.plus(Position.create(3,2)))
                .build()

        screen.addLayer(ledgerLayer)

        // score cursor layer
        scoreCursorLayer = ledgerLayer.createCopy()

        screen.addLayer(scoreCursorLayer)

        // note layer
        noteLayer = ledgerLayer.createCopy()

        screen.addLayer(noteLayer)

        // controls
        controlPanel = Components.vbox()
            .withSize(scoreContainer.width, scoreContainer.height - scorePanel.height)
            .withSpacing(1)
            .withDecorations(box(BoxType.SINGLE))
            .build()

        scoreContainer.addComponent(controlPanel)

        // title
        val titleLabel = Components.label()
            .withText(" Title:  ")
            .build()

        val titleInput = Components.textArea()
            .withSize(30,1)
            .build()

        val titleBox = Components.hbox()
            .withSize(controlPanel.width - 8,1)
            .build()

        titleBox.addComponent(titleLabel)
        titleBox.addComponent(titleInput)

        // artist
        val artistLabel = Components.label()
            .withText(" Artist: ")
            .build()

        val artistInput = Components.textArea()
            .withSize(30,1)
            .build()

        val artistBox = Components.hbox()
            .withSize(controlPanel.width - 8,1)
            .build()

        artistBox.addComponent(artistLabel)
        artistBox.addComponent(artistInput)

        // bpm
        val bpmLabel = Components.label()
            .withText("BPM:    ")
            .build()

        val bpmInput = Components.textArea()
            .withSize(30,1)
            .build()

        val bpmBox = Components.hbox()
            .withSize(controlPanel.width - 8,1)
            .build()

        // invisible component to take focus away
        // since Component.clearFocus() doesn't work
        val invisBtn = Components.toggleButton()
            .withSize(1,1)
            .build()

        bpmBox.addComponent(invisBtn)
        bpmBox.addComponent(bpmLabel)
        bpmBox.addComponent(bpmInput)

        controlPanel.addComponent(titleBox)
        controlPanel.addComponent(artistBox)
        controlPanel.addComponent(bpmBox)

        screen.display()
    }

    private fun drawStaff()
    {
        val startPos = scorePanel.absolutePosition.plus(Position.create(3,6))

        // draw staff lines
        val line = Shapes.buildLine(
                Position.zero(),
                Position.create(STAFF_LENGTH-1, 0))
                .toTileGraphics(Tile.newBuilder()
                        .withCharacter(Symbols.SINGLE_LINE_HORIZONTAL)
                        .withBackgroundColor(TileColor.transparent())
                        .withForegroundColor(ANSITileColor.WHITE)
                        .build(),
                        screen.tileset
                )

        // active staff
        for (i in 0..4) {
            val pos = startPos.plus(Position.create(0, i * 2))
            screen.draw(line, pos)
        }

        // next staff
        for (i in 0..2) {
            val pos = startPos.plus(Position.create(0, 17 + (i * 2)))
            screen.draw(line, pos)
        }
    }

    fun drawScore(comp: CompositionModel, cursorPos: Int = 0)
    {
        // clear layers
        noteLayer.clear()
        scoreCursorLayer.clear()
        ledgerLayer.clear()

        // redraw
        var position = 0

        for ((i,note) in comp.melody.withIndex()){
            position += drawNote(note, position, i == cursorPos)
        }
    }

    fun drawNote(note: CompositionModel.Note, xPos: Int, isActive: Boolean = false): Int
    {
        val noteGraphic = NoteGraphics.getNoteGraphic(note)

        // calculate appropriate staff line position
        var yPos = noteStaffMap[note.name]!! + ((5 - note.octave) * 7)

        // check if ledger line needed
        if (note.name == 'C' && note.octave == 4){
            ledgerLayer.draw(NoteGraphics.ledgerLine, Position.create(xPos, 14))
        }
        if (note.octave == 5 && note.name in listOf('A', 'B')){
            ledgerLayer.draw(NoteGraphics.ledgerLine, Position.create(xPos, 2))
        }
        if (note.octave == 6){
            ledgerLayer.draw(NoteGraphics.ledgerLine, Position.create(xPos, 0))
            ledgerLayer.draw(NoteGraphics.ledgerLine, Position.create(xPos, 2))
        }

        // add note head offset
        yPos += NoteGraphics.getNoteHeadOffset(note)

        // check if highlight needed
        if (isActive){
            scoreCursorLayer.draw(NoteGraphics.cursor, Position.create(xPos, yPos))
        }

        noteLayer.draw(noteGraphic, Position.create(xPos, yPos))
        return noteGraphic.width
    }

    fun focusInput(index: Int)
    {
        if (index in 0..2) {
            val inputs = controlPanel.children.map { (it as HBox).children.last() }

            inputs[index].requestFocus()
        }
    }

    fun clearInputFocus()
    {
        val throwaway = (controlPanel.children.last() as HBox).children.first()
        throwaway.requestFocus()
    }
}