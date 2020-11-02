package io.github.sunshinewzy.sunnybot.enums

enum class RunningState(val gameName: String = "", val isMain: Boolean = true) {
    FREE,
    HOUR24("24µã"),
    TICTACTOE("¾®×ÖÆå"),
    TICTACTOE_WAITING("¾®×ÖÆå", false)

}