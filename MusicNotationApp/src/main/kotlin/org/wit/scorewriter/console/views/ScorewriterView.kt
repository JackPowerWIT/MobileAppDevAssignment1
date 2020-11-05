package org.wit.scorewriter.console.views

import org.hexworks.zircon.api.*
import org.hexworks.zircon.api.ComponentDecorations.box
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.color.ANSITileColor
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.api.component.Container
import org.hexworks.zircon.api.component.Panel
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.extensions.toScreen
import org.hexworks.zircon.api.graphics.*
import org.hexworks.zircon.api.screen.Screen
import org.wit.scorewriter.console.models.CompositionModel

class ScorewriterView {

    private val screen: Screen
    private val scorePanel: Panel
    private val ledgerLayer: Layer
    private val noteLayer: Layer

    private val STAFF_LENGTH: Int

    private val noteStaffMap = mapOf<Char, Int>(
            'B' to 1, 'A' to 2, 'G' to 3, 'F' to 4, 'E' to 5, 'D' to 6, 'C' to 7
    )

    init {
        val tileGrid = SwingApplications.startTileGrid(
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
        val libraryPanel = Components.vbox()
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

        // note layer
        noteLayer = ledgerLayer.createCopy()

        screen.addLayer(noteLayer)

        // controls
        val controlPanel = Components.hbox()
                .withSize(scoreContainer.width, scoreContainer.height - scorePanel.height)
                .withDecorations(box(BoxType.SINGLE))
                .build()

        scoreContainer.addComponent(controlPanel)

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

    fun drawScore(comp: CompositionModel)
    {
        var position = 0

        for (note in comp.melody){
            position += drawNote(note, position)
        }
    }

    fun drawNote(note: CompositionModel.Note, xPos: Int): Int
    {
        val noteGraphic = NoteGraphics.getNoteGraphic(note)

        // calculate appropriate staff line position
        var yPos = noteStaffMap[note.name]!! + ((5 - note.octave) * 7)

        // check if ledger line needed
        if (note.name.toString().plus(note.octave) in listOf("C4", "A5", "C6")){
            ledgerLayer.draw(NoteGraphics.ledgerLine, Position.create(xPos, yPos))
        }

        // add note head offset
        yPos += NoteGraphics.getNoteHeadOffset(note)

        noteLayer.draw(noteGraphic, Position.create(xPos, yPos))
        return noteGraphic.width
    }
}