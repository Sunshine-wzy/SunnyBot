package io.github.sunshinewzy.sunnybot.enums

enum class RunningState(val gameName: String = "", val isMain: Boolean = true) {
    FREE,
    HOUR24("24��"),
    TICTACTOE("������"),
    TICTACTOE_WAITING("������", false)

}