package io.github.sunshinewzy.sunnybot.runnable

import io.github.sunshinewzy.sunnybot.SunnyBot
import io.github.sunshinewzy.sunnybot.functions.DailySignIn
import io.github.sunshinewzy.sunnybot.sunnyScope
import kotlinx.coroutines.launch
import java.util.*

object STimerTask: TimerTask() {
    override fun run() {
        sunnyScope.launch { 
            // 每日签到更新
            DailySignIn.runSchedule()
            
            // 语音下载
            SunnyBot.downloadVoice()
        }
    }
}