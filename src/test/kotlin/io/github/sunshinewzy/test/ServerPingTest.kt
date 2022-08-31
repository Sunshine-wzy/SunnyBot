package io.github.sunshinewzy.test

import io.github.sunshinewzy.sunnybot.module.server.ping.ServerPing
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.net.InetSocketAddress


object ServerPingTest {
    
    @Test
    fun ping() {
        val address = InetAddress.getByName("happylandmc.cc")
        val port = 25565
        val ping = ServerPing(InetSocketAddress(address, port))
        val response = ping.fetchData()
        println(response.players.online)
    }
    
}