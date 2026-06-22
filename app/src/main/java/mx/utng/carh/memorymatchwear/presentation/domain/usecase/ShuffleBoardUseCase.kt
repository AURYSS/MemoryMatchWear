package mx.utng.carh.memorymatchwear.presentation.domain.usecase

import mx.utng.carh.memorymatchwear.presentation.domain.model.Card
import mx.utng.carh.memorymatchwear.presentation.domain.model.CardSymbol

class ShuffleBoardUseCase {
    /** Crea 12 tarjetas (2 de cada símbolo) mezcladas aleatoriamente. */
    operator fun invoke(): List<Card> =
        CardSymbol.values()
            .flatMap { symbol -> listOf(symbol, symbol) } // duplicar cada símbolo
            .shuffled()                                   // mezclar
            .mapIndexed { index, symbol ->                // asignar id
                Card(id = index, symbol = symbol)
            }
}

