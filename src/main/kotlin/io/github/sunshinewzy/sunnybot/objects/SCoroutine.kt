package io.github.sunshinewzy.sunnybot.objects

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object SCoroutine {
    val http = Executors.newCachedThreadPool().asCoroutineDispatcher()
    val download = Executors.newCachedThreadPool().asCoroutineDispatcher()
}