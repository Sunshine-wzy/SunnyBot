package io.github.sunshinewzy.sunnybot.enums

enum class RunningState(val gameName: String = "", val isMain: Boolean = true) {
    FREE,
    HOUR24("24��"),
    TICTACTOE("������"),
    TICTACTOE_WAITING("������", false),
    CHESS("Χ��"),
    CHESS_WAITING("Χ��", false),
    FIVE_IN_A_ROW("������"),
    FIVE_IN_A_ROW_WAITING("������", false),
    
}