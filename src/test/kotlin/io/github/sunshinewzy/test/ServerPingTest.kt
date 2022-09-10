package io.github.sunshinewzy.test

import io.github.sunshinewzy.sunnybot.module.server.ping.SServerPing
import io.github.sunshinewzy.sunnybot.module.server.ping.ServerPing
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.net.InetSocketAddress


object ServerPingTest {
    
    @Test
    fun ping() {
        val port = 25565
        val newIp = SServerPing.resolveIp("mc.hypixel.net", port) ?: kotlin.run { 
            println("Failed.")
            return
        }
        
        val address = InetAddress.getByName(newIp.first)
        val ping = ServerPing(InetSocketAddress(address, newIp.second))
        val response = ping.fetchData()
        println(response.players.online)
    }

}