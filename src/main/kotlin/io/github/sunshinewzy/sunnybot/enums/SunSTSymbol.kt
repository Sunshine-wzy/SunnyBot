package io.github.sunshinewzy.sunnybot.enums

enum class SunSTSymbol(val oldContent: String, var newContent: String) {
    ENTER("\n", "ENTER"),
    ;
    
    init {
        newContent = "<SunST: $newContent>"
    }
    
}