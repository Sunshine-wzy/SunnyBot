package io.github.sunshinewzy.sunnybot.functions

import io.github.sunshinewzy.sunnybot.miraiBot
import io.github.sunshinewzy.sunnybot.objects.SSavePlayer
import io.github.sunshinewzy.sunnybot.objects.getSGroup

object DailySignIn {
    suspend fun runSchedule() {
//        val dateFormat = SimpleDateFormat("yyyy.MM.dd")
//        val day = dateFormat.format(Date())
//        if(SSaveSunny.dailySignInUpdate == day)
//            return

        miraiBot?.groups?.forEach { 
            it.getSGroup().dailySignIns.clear()
            
//            it.sendMessage("""
//                �µ�һ�쿪ʼ��~
//                ��λ����ǩ������Ŷ
//                ��д�����Ľ������ԣ�
//                
//                /ǩ�� <���Ľ�������>
//            """.trimIndent())
        }

        SSavePlayer.sPlayerMap.values.forEach { 
            it.isDailySignIn = false
        }

//        SSaveSunny.dailySignInUpdate = day
    }
}