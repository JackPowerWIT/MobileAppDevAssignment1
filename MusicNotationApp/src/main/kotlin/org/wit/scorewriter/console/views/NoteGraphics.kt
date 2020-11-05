package org.wit.scorewriter.console.views

import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.DrawSurfaces
import org.hexworks.zircon.api.Shapes
import org.hexworks.zircon.api.builder.graphics.TileCompositeBuilder
import org.hexworks.zircon.api.color.ANSITileColor
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.graphics.Symbols
import org.hexworks.zircon.api.graphics.TileComposite
import org.wit.scorewriter.console.models.CompositionModel

object NoteGraphics {

    // tiles
    private val tileBuilder = Tile.newBuilder()
            .withForegroundColor(ANSITileColor.BRIGHT_WHITE)
            .withBackgroundColor(TileColor.transparent())

    private val noteHeadLeft = tileBuilder
            .withCharacter('(')
            .build()

    private val noteHeadRight = tileBuilder
            .withCharacter(')')
            .build()

    private val noteStem = tileBuilder
            .withCharacter(Symbols.SINGLE_LINE_VERTICAL)
            .build()

    private val noteFlagTop = tileBuilder
            .withCharacter(Symbols.SINGLE_LINE_TOP_LEFT_CORNER)
            .build()

    private val noteFlagMiddle = tileBuilder
            .withCharacter(Symbols.SINGLE_LINE_T_RIGHT)
            .build()

    private val noteFlagBottom = tileBuilder
            .withCharacter(Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER)
            .build()

    private val sharp = tileBuilder
            .withCharacter('#')
            .build()

    private val ledger = Tile.newBuilder()
            .withForegroundColor(ANSITileColor.WHITE)
            .withBackgroundColor(TileColor.transparent())
            .withCharacter(Symbols.SINGLE_LINE_HORIZONTAL)
            .build()

    private val cursorTile = Tile.newBuilder()
        .withForegroundColor(ANSITileColor.CYAN)
        .withBackgroundColor(TileColor.defaultBackgroundColor())
        .withCharacter(Symbols.BLOCK_SOLID)
        .build()

    // composites
    private val sixteenthNoteUp = TileCompositeBuilder.newBuilder()
            .withSize(Size.create(3,4))
            .withTile(Position.create(2,0), noteFlagTop)
            .withTile(Position.create(2,1), noteFlagMiddle)
            .withTile(Position.create(2,2), noteStem)
            .withTile(Position.create(1,3), noteHeadLeft)
            .withTile(Position.create(2,3), noteHeadRight)
            .build()

    private val sixteenthNoteDown = TileCompositeBuilder.newBuilder()
            .withSize(Size.create(3,4))
            .withTile(Position.create(1,0), noteHeadLeft)
            .withTile(Position.create(2,0), noteHeadRight)
            .withTile(Position.create(1,1), noteStem)
            .withTile(Position.create(1,2), noteFlagMiddle)
            .withTile(Position.create(1,3), noteFlagBottom)
            .build()

    private val eigthNoteUp = TileCompositeBuilder.newBuilder()
            .withSize(Size.create(6,4))
            .withTiles(sixteenthNoteUp.tiles)
            .withTile(Position.create(2,1), noteStem)
            .build()

    private val eigthNoteDown = TileCompositeBuilder.newBuilder()
            .withSize(Size.create(6,4))
            .withTiles(sixteenthNoteDown.tiles)
            .withTile(Position.create(1,2), noteStem)
            .build()

    private val quarterNoteUp = TileCompositeBuilder.newBuilder()
            .withSize(Size.create(12,4))
            .withTiles(eigthNoteUp.tiles)
            .withTile(Position.create(2,0), noteStem)
            .build()

    private val quarterNoteDown = TileCompositeBuilder.newBuilder()
            .withSize(Size.create(12,4))
            .withTiles(eigthNoteDown.tiles)
            .withTile(Position.create(1,3), noteStem)
            .build()

    val ledgerLine = Shapes.buildLine(
            Position.zero(),
            Position.create(3,0))
            .toTileGraphics(ledger,
                    CP437TilesetResources.rexPaint16x16())

    val cursor = Shapes.buildFilledRectangle(
        Position.zero(),
        Size.create(3,4))
        .toTileGraphics(cursorTile,
            CP437TilesetResources.rexPaint16x16())

    // 2D array organised by duration and stem direction
    private val notesStemUp = arrayOf(quarterNoteUp, eigthNoteUp, sixteenthNoteUp)
    private val notesStemDown = arrayOf(quarterNoteDown, eigthNoteDown, sixteenthNoteDown)
    private val notes = arrayOf(notesStemUp, notesStemDown)

    fun getNoteGraphic(note: CompositionModel.Note): TileComposite
    {
        // use a downward stem if the pitch is higher than B4
        val stemChoice = if (note.octave > 4) 1 else 0
        // map 4,8,16 to 0,1,2
        val durationChoice = (note.duration / 8)

        var noteGraphic = notes[stemChoice][durationChoice]

        if (note.isSharp){
            val headHeight = 3 - (3 * stemChoice)
            noteGraphic = TileCompositeBuilder.newBuilder()
                    .withSize(noteGraphic.size)
                    .withTiles(noteGraphic.tiles)
                    .withTile(Position.create(0,headHeight), sharp)
                    .build()
        }

        return noteGraphic
    }

    // get offset to place note head correctly
    fun getNoteHeadOffset(note: CompositionModel.Note): Int{
        return if (note.octave > 4) 0 else -3
    }
}