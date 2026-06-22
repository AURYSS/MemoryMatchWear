package mx.utng.carh.memorymatchwear.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mx.utng.carh.memorymatchwear.presentation.domain.model.GamePhase
import mx.utng.carh.memorymatchwear.presentation.domain.model.GameState
import mx.utng.carh.memorymatchwear.presentation.domain.repository.BestTimeRepository
import mx.utng.carh.memorymatchwear.presentation.domain.usecase.CheckMatchUseCase
import mx.utng.carh.memorymatchwear.presentation.domain.usecase.FlipCardUseCase
import mx.utng.carh.memorymatchwear.presentation.domain.usecase.MatchResult
import mx.utng.carh.memorymatchwear.presentation.domain.usecase.ShuffleBoardUseCase

class MemoryViewModel(
    private val shuffleBoard  : ShuffleBoardUseCase,
    private val flipCard      : FlipCardUseCase,
    private val checkMatch    : CheckMatchUseCase,
    private val saveBestTime  : SaveBestTimeUseCase,
    private val getBestTime   : GetBestTimeUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects: Flow<GameEffect> = _effects.receiveAsFlow()

    private var timerJob: Job? = null

    init { startNewGame() }

    fun startNewGame() {
        timerJob?.cancel()
        val board = shuffleBoard()
        val bestTime = runBlocking { getBestTime() }
        _state.value = GameState(
            board = board,
            phase = GamePhase.SELECTING_FIRST,
            bestTime = bestTime
        )
        startTimer()
    }

    fun onCardTapped(cardIndex: Int) {
        val current = _state.value
        if (current.phase == GamePhase.CHECKING || current.phase == GamePhase.WON) return

        val afterFlip = flipCard(current, cardIndex)
        _state.value = afterFlip

        if (afterFlip.phase == GamePhase.CHECKING) {
            evaluateMatch(afterFlip)
        }
    }

    private fun evaluateMatch(state: GameState) {
        viewModelScope.launch {
            delay(800L)
            when (checkMatch(state)) {
                MatchResult.HIT -> {
                    val newState = applyMatch(state)
                    _state.value = newState
                    _effects.send(GameEffect.HapticMatch)
                    if (newState.isComplete) onGameWon(newState)
                }
                MatchResult.MISS -> {
                    _state.value = flipBothBack(state)
                    _effects.send(GameEffect.HapticMiss)
                }
                MatchResult.PENDING -> Unit
            }
        }
    }

    private fun applyMatch(state: GameState): GameState {
        val first  = state.firstSelected ?: return state
        val second = state.secondSelected ?: return state
        val newBoard = state.board.mapIndexed { i, card ->
            if (i == first || i == second) card.copy(isMatched = true) else card
        }
        val matches = state.matchesFound + 1
        return state.copy(
            board          = newBoard,
            matchesFound   = matches,
            firstSelected  = null,
            secondSelected = null,
            phase          = if (matches == GameState.TOTAL_PAIRS)
                GamePhase.WON else GamePhase.SELECTING_FIRST
        )
    }

    private fun flipBothBack(state: GameState): GameState {
        val first  = state.firstSelected ?: return state
        val second = state.secondSelected ?: return state
        val newBoard = state.board.mapIndexed { i, c ->
            if (i == first || i == second) c.copy(isFlipped = false) else c
        }
        return state.copy(
            board = newBoard,
            firstSelected = null,
            secondSelected = null,
            phase = GamePhase.SELECTING_FIRST
        )
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private suspend fun onGameWon(state: GameState) {
        timerJob?.cancel()
        saveBestTime(state.elapsedSeconds)
        _effects.send(GameEffect.HapticVictory)
    }

    override fun onCleared() { timerJob?.cancel() }
}

sealed class GameEffect {
    data object HapticMatch   : GameEffect()
    data object HapticMiss    : GameEffect()
    data object HapticVictory : GameEffect()
}

// UseCases definidos aquí para evitar crear múltiples archivos
class GetBestTimeUseCase(private val repository: BestTimeRepository) {
    suspend operator fun invoke(): Long = repository.getBestTime()
}

class SaveBestTimeUseCase(private val repository: BestTimeRepository) {
    suspend operator fun invoke(seconds: Long) = repository.saveBestTime(seconds)
}
