package io.github.sunshinewzy.sunnybot.objects

class SCoordinate(var x: Int, var y: Int) {
    operator fun plus(coordinate: SCoordinate): SCoordinate =
        SCoordinate(x + coordinate.x, y + coordinate.y)
    
    operator fun plusAssign(coordinate: SCoordinate) {
        x += coordinate.x
        y += coordinate.y
    }
}