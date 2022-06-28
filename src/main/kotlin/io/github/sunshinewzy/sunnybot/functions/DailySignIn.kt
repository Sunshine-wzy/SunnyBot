package io.github.sunshinewzy.sunnybot.functions

import io.github.sunshinewzy.sunnybot.objects.SSavePlayer
import io.github.sunshinewzy.sunnybot.objects.getSGroup
import io.github.sunshinewzy.sunnybot.sunnyBot

object DailySignIn {
    fun reset() {
//        val dateFormat = SimpleDateFormat("yyyy.MM.dd")
//        val day = dateFormat.format(Date())
//        if(SSaveSunny.dailySignInUpdate == day)
//            return

        sunnyBot.groups.forEach {
            it.getSGroup().dailySignIns.clear() 
        
        //            it.sendMessage("""
        //                新的一天开始了~
        //                各位可以签到打卡了哦
        //                请写下您的今日赠言！
        //                
        //                /签到 <您的今日赠言>
        //            """.trimIndent())
        }

        SSavePlayer.sPlayerMap.values.forEach { 
            it.isDailySignIn = false
        }

//        SSaveSunny.dailySignInUpdate = day
    }
}