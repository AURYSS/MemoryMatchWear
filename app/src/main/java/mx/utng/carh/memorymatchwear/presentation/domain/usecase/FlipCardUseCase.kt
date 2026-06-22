package mx.utng.carh.memorymatchwear.presentation.domain.usecase

import androidx.room.util.copy
import mx.utng.carh.memorymatchwear.presentation.domain.model.GamePhase
import mx.utng.carh.memorymatchwear.presentation.domain.model.GameState

class FlipCardUseCase {
    /**
     * Retorna el nuevo GameState con la tarjeta volteada,
     * o el mismo estado si el toque no es válido.
     */
    operator fun invoke(state: GameState, cardIndex: Int): GameState {
        val card = state.board[cardIndex]
        // Ignorar si ya está revelada o ya fue encontrada
        if (card.isFlipped || card.isMatched) return state
        // Ignorar si ya hay 2 tarjetas seleccionadas
        if (state.secondSelected != null) return state

        val newBoard = state.board.mapIndexed { i, c ->
            if (i == cardIndex) c.copy(isFlipped = true) else c
        }

        return when {
            state.firstSelected == null -> state.copy(
                board = newBoard,
                firstSelected = cardIndex,
                phase = GamePhase.WAITING_SECOND
            )
            else -> state.copy(
                board = newBoard,
                secondSelected = cardIndex,
                phase = GamePhase.CHECKING,
                moves = state.moves + 1
            )
        }
    }
}
