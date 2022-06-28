package io.github.sunshinewzy.sunnybot.timer

import io.github.sunshinewzy.sunnybot.commands.SCGaoKaoCountDown
import io.github.sunshinewzy.sunnybot.functions.DailySignIn
import io.github.sunshinewzy.sunnybot.objects.SSaveGroup
import io.github.sunshinewzy.sunnybot.sendGroupMsg
import io.github.sunshinewzy.sunnybot.sunnyBot
import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.data.toPlainText
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
                    SSaveGroup.sGroupMap.forEach { (groupId, sGroup) ->
                        if(second == 0) {
                            if(minute == 0) {
                                if(hour == 0) {
                                    DailySignIn.reset()
                                } else if(hour == 9) {
                                    if(sGroup.isGaoKaoCountDown) {
                                        sunnyBot.sendGroupMsg(groupId, "高考倒计时每日提醒", SCGaoKaoCountDown.getCountDownContent() + "\n\nTip: 发送 /gk on 或 /gk off\n  以 开启/关闭 高考倒计时每日提醒")
                                    }
                                }
                            }

                            val remListReminders = arrayListOf<Int>()
                            sGroup.reminders.forEachIndexed { index, it ->
                                if(hour == it.hour && minute == it.minute) {
                                    sunnyBot.sendGroupMsg(groupId, "定时提醒", if(it.isAtAll) AtAll + " " + it.content else it.content.toPlainText())
                                    if(it.isOnce) remListReminders += index
                                }
                            }
                            
                            for(i in remListReminders.size - 1 downTo 0)
                                sGroup.reminders.removeAt(remListReminders[i])
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