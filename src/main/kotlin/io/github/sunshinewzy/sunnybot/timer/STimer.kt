package io.github.sunshinewzy.sunnybot.timer

import io.github.sunshinewzy.sunnybot.commands.SCGaoKaoCountDown
import io.github.sunshinewzy.sunnybot.objects.SSaveGroup
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.sunnyBot
import io.github.sunshinewzy.sunnybot.sunnyScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Group
import java.lang.Exception
import java.util.*

object STimer : Runnable {
    private val time = STime()
    var calendar = Calendar.getInstance()
        private set
    
    override fun run() {
        synchronized(Unit) {
            while(true) {
                val c = Calendar.getInstance()
                calendar = c
                time.year = c.get(Calendar.YEAR)
                time.month = c.get(Calendar.MONTH) + 1
                time.date = c.get(Calendar.DATE)
                time.hour = c.get(Calendar.HOUR_OF_DAY)
                time.minute = c.get(Calendar.MINUTE)
                time.second = c.get(Calendar.SECOND)
                
                
                time.apply {
                    if(hour == 9 && minute == 0 && second == 0) {
                        SSaveGroup.sGroupMap.forEach { groupId, sGroup -> 
                            if(sGroup.isGaoKaoCountDown) {
                                sunnyBot.getGroup(groupId)?.apply { 
                                    sunnyScope.launch {
                                        sendMsg("高考倒计时每日提醒", SCGaoKaoCountDown.getCountDownContent() + "\n\nTip: 发送 /gk on 或 /gk off\n  以 开启/关闭 高考倒计时每日提醒")
                                    }
                                }
                            }
                        }
                    }
                    
                }
                
                
                try {
                    Thread.sleep(1000)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }
    
    
    fun getTime(): STime = time.copy()
}