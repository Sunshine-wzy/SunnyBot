package io.github.sunshinewzy.sunnybot.functions

import io.github.sunshinewzy.sunnybot.antiRecall
import io.github.sunshinewzy.sunnybot.miraiBot
import io.github.sunshinewzy.sunnybot.objects.addSTD
import io.github.sunshinewzy.sunnybot.objects.getSGroup
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import kotlin.random.Random

object Repeater {
    private val repeatMap = mutableMapOf<Long, Boolean>()
    
    
    fun repeat() {
        miraiBot?.subscribeGroupMessages {
            always { 
                if(!group.getSGroup().isRepeat) return@always
                val groupMap = antiRecall?.groupMap ?: return@always
                val list = groupMap[group.id]?.second ?: return@always
                
                var cnt = 0
                list.forEach { 
                    val msg = it.third
                    if(message.contentEquals(msg)) cnt++
                }
                
                if(Random.nextInt(100) + 1 <= 10 * cnt){
                    reply(message)
                    
                    if(Random.nextInt(100) + 1 <= 10){
                        reply("����ı����ǡ���")
                        repeatMap[group.id] = true
                    }
                }
            }
            
            contains("������") {
                if(repeatMap[group.id] == true){
                    sender.addSTD(5)
                    reply(At(sender) + """
                        
                        û������ı����Ǹ�����
                        ������ 5 STD~
                    """.trimIndent())
                    
                    repeatMap[group.id] = false
                }
            }
        }
    }
    
}