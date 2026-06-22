package mx.utng.carh.memorymatchwear.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import mx.utng.carh.memorymatchwear.presentation.board.BoardScreen
import mx.utng.carh.memorymatchwear.presentation.board.GetBestTimeUseCase
import mx.utng.carh.memorymatchwear.presentation.board.MemoryViewModel
import mx.utng.carh.memorymatchwear.presentation.board.SaveBestTimeUseCase
import mx.utng.carh.memorymatchwear.presentation.data.datasource.BestTimeDataSource
import mx.utng.carh.memorymatchwear.presentation.domain.repository.BestTimeRepositoryImpl
import mx.utng.carh.memorymatchwear.presentation.domain.usecase.CheckMatchUseCase
import mx.utng.carh.memorymatchwear.presentation.domain.usecase.FlipCardUseCase
import mx.utng.carh.memorymatchwear.presentation.domain.usecase.ShuffleBoardUseCase

class MemoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dataSource  = BestTimeDataSource(context)
        val repository  = BestTimeRepositoryImpl(dataSource)
        return MemoryViewModel(
            shuffleBoard = ShuffleBoardUseCase(),
            flipCard = FlipCardUseCase(),
            checkMatch = CheckMatchUseCase(),
            saveBestTime = SaveBestTimeUseCase(repository),
            getBestTime = GetBestTimeUseCase(repository),
        ) as T
    }
}

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: MemoryViewModel = viewModel(
                factory = MemoryViewModelFactory(applicationContext)
            )
            MaterialTheme { BoardScreen(viewModel = vm) }
        }
    }
}

