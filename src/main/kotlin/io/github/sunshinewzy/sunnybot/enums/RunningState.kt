package io.github.sunshinewzy.sunnybot.enums

enum class RunningState(val gameName: String = "", val isMain: Boolean = true) {
    FREE,
    HOUR24("24µã"),
    TICTACTOE("¾®×ÖÆå"),
    TICTACTOE_WAITING("¾®×ÖÆå", false),
    CHESS("Î§Æå"),
    CHESS_WAITING("Î§Æå", false),
    FIVE_IN_A_ROW("Îå×ÓÆå"),
    FIVE_IN_A_ROW_WAITING("Îå×ÓÆå", false),
    
}