package io.github.sunshinewzy.sunnybot.timer

import io.github.sunshinewzy.sunnybot.*
import io.github.sunshinewzy.sunnybot.commands.SCGaoKaoCountDown
import io.github.sunshinewzy.sunnybot.functions.DailySignIn
import io.github.sunshinewzy.sunnybot.objects.SSaveGroup
import io.github.sunshinewzy.sunnybot.utils.SImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.data.toPlainText
import java.util.*
import kotlin.random.Random

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
                    if(second == 0) {
                        SSaveGroup.sGroupMap.forEach { (groupId, sGroup) ->
                            if(minute == 0) {
                                if(hour == 0) {
                                    DailySignIn.reset()
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
                        
                        
                        if(minute == 0) {
                            if(hour == 9) {
                                sunnyScope.launch(Dispatchers.IO) {
                                    val imageGaoKaoCountDown = SImage.showTextWithSilverBackground(SCGaoKaoCountDown.getCountDownContent() + "\n\nTip: 发送 /gk on 或 /gk off\n  以 开启/关闭 高考倒计时每日提醒")
                                    
                                    SSaveGroup.sGroupMap.forEach { (groupId, sGroup) ->
                                        if(sGroup.isGaoKaoCountDown) {
                                            sunnyBot.getGroup(groupId)?.let { 
                                                it.sendMsg(
                                                    SCGaoKaoCountDown.description,
                                                    imageGaoKaoCountDown.uploadAsImage(it) ?: "图片渲染失败".toPlainText()
                                                )
                                            }
                                            
                                            delay(Random.nextLong(100, 200))
                                        }
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