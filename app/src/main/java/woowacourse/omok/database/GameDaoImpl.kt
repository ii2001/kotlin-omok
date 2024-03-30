package woowacourse.omok.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import woowacourse.omok.model.board.Stone
import woowacourse.omok.model.omokGame.Board.Companion.BOARD_SIZE

class GameDaoImpl(private val dbHelper: DatabaseHelper) : GameDao {

    override fun saveGame(board: Array<Array<Stone>>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("GameBoard", null, null) // 기존 데이터 삭제
            for (i in board.indices) {
                for (j in board[i].indices) {
                    val stone = board[i][j]
                    if (stone != Stone.EMPTY) {
                        val stoneType = when (stone) {
                            Stone.BLACK -> 1
                            Stone.WHITE -> 2
                            else -> 0
                        }
                        val values = ContentValues().apply {
                            put("rowIndex", i)
                            put("columnIndex", j)
                            put("stoneType", stoneType)
                        }
                        db.insert("GameBoard", null, values)
                    }

                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun loadGame(): Array<Array<Stone>> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "GameBoard",
            arrayOf("rowIndex", "columnIndex", "stoneType"),
            null, null, null, null, null
        )
        val board = Array(BOARD_SIZE) { Array(BOARD_SIZE) { Stone.EMPTY } }
        with(cursor) {
            while (moveToNext()) {
                val rowIndex = getInt(getColumnIndexOrThrow("rowIndex"))
                val columnIndex = getInt(getColumnIndexOrThrow("columnIndex"))
                val stoneType = getInt(getColumnIndexOrThrow("stoneType"))
                val stone = when (stoneType) {
                    1 -> Stone.BLACK
                    2 -> Stone.WHITE
                    else -> Stone.EMPTY
                }
                board[rowIndex][columnIndex] = stone
            }
            close()
        }
        return board
    }

    override fun resetGame() {
        val db = dbHelper.writableDatabase
        db.execSQL("DELETE FROM GameBoard")
    }

    override fun saveCurrentStone(currentStone: Int) {
        val db = resetCurrentStone()
        val values = ContentValues().apply {
            put("currentStone", currentStone)
        }
        db.insert("GameStatus", null, values)
    }

    private fun resetCurrentStone(): SQLiteDatabase {
        val db = dbHelper.writableDatabase
        db.execSQL("DELETE FROM GameStatus")
        return db
    }

    override fun loadCurrentStone(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.query("GameStatus", arrayOf("currentStone"), null, null, null, null, null)
        var stoneType = -1
        val columnIndex = cursor.getColumnIndex("currentStone")
        if (columnIndex != -1 && cursor.moveToFirst()) {
            stoneType = cursor.getInt(columnIndex)
        }
        cursor.close()
        return stoneType
    }
}
