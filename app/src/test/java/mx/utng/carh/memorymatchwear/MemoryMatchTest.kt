package mx.utng.carh.memorymatchwear

import mx.utng.carh.memorymatchwear.presentation.domain.model.CardSymbol
import mx.utng.carh.memorymatchwear.presentation.domain.model.GamePhase
import mx.utng.carh.memorymatchwear.presentation.domain.model.GameState
import mx.utng.carh.memorymatchwear.presentation.domain.usecase.CheckMatchUseCase
import mx.utng.carh.memorymatchwear.presentation.domain.usecase.FlipCardUseCase
import mx.utng.carh.memorymatchwear.presentation.domain.usecase.MatchResult
import mx.utng.carh.memorymatchwear.presentation.domain.usecase.ShuffleBoardUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryMatchTest {
    private val shuffle = ShuffleBoardUseCase()
    private val flip    = FlipCardUseCase()
    private val check   = CheckMatchUseCase()
 
    @Test fun `board has 12 cards`() {
        assertEquals(12, shuffle().size)
    }
 
    @Test fun `board has exactly 2 of each symbol`() {
        val board = shuffle()
        CardSymbol.values().forEach { symbol ->
            assertEquals(2, board.count { it.symbol == symbol })
        }
    }
 
    @Test fun `flip sets card isFlipped to true`() {
        val board = shuffle()
        val state = GameState(board=board, phase=GamePhase.SELECTING_FIRST)
        val next  = flip(state, 0)
        assertTrue(next.board[0].isFlipped)
        assertEquals(0, next.firstSelected)
    }
 
    @Test fun `checkMatch returns HIT when symbols match`() {
        val board = shuffle().toMutableList()
        // Forzar mismo símbolo en posiciones 0 y 1
        board[0] = board[0].copy(symbol=CardSymbol.COMPOSE, isFlipped=true)
        board[1] = board[1].copy(symbol=CardSymbol.COMPOSE, isFlipped=true)
        val state = GameState(board=board, firstSelected=0, secondSelected=1)
        assertEquals(MatchResult.HIT, check(state))
    }
 
    @Test fun `checkMatch returns MISS when symbols differ`() {
        val board = shuffle().toMutableList()
        board[0] = board[0].copy(symbol=CardSymbol.COMPOSE, isFlipped=true)
        board[1] = board[1].copy(symbol=CardSymbol.ROOM,    isFlipped=true)
        val state = GameState(board=board, firstSelected=0, secondSelected=1)
        assertEquals(MatchResult.MISS, check(state))
    }
}
