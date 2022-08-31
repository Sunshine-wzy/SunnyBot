package io.github.sunshinewzy.sunnybot.module.server.ping

import io.github.sunshinewzy.sunnybot.commands.SCServerInfo.description
import io.github.sunshinewzy.sunnybot.enums.ServerType
import io.github.sunshinewzy.sunnybot.enums.ServerType.*
import io.github.sunshinewzy.sunnybot.objects.SBBEServerPing
import io.github.sunshinewzy.sunnybot.objects.SRequest
import io.github.sunshinewzy.sunnybot.sendMsg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.buildMessageChain
import java.net.InetAddress
import java.net.InetSocketAddress

object SServerPing {
    const val apiBEServer = "http://motdpe.blackbe.xyz/api.php"
    
    
    suspend fun Contact.pingServer(
        ip: String,
        type: ServerType,
        isDetailed: Boolean = false,
        isPlayerSample: Boolean = false
    ) {
        when(type) {
            JAVA_EDITION -> {
                pingServerJavaEdition(ip, isDetailed, isPlayerSample)
            }
            
            BEDROCK_EDITION -> {
                pingServerBedrockEdition(ip, isDetailed)
            }
            
            ALL -> {
                if(!pingServerJavaEdition(ip, isDetailed, isPlayerSample)) {
                    pingServerBedrockEdition(ip, isDetailed)
                }
            }

            NOT -> {}
        }
    }
    
    suspend fun Contact.pingServerJavaEdition(
        ip: String,
        isDetailed: Boolean = false,
        isPlayerSample: Boolean = false
    ): Boolean {
        val (hostname, port) = ip.toIpAndPort() ?: kotlin.run {
            sendMsg(description, "服务器IP格式错误")
            return false
        }

        return withContext(Dispatchers.IO) {
            val ping = kotlin.runCatching {
                val address = InetAddress.getByName(hostname)
                ServerPing(InetSocketAddress(address, port))
            }.onFailure {
                sendMsg(description, "无法连接到服务器")
            }.getOrNull() ?: return@withContext false
            
            val response = runCatching { 
                ping.fetchData()
            }.onFailure {
                sendMsg(description, "无法获取服务器状态")
            }.getOrNull() ?: return@withContext false

            val image = response.faviconInputStream?.use {
                uploadImage(it)
            } ?: kotlin.run {
                sendMsg(description, "服务器图标获取失败")
                return@withContext false
            }

            sendMsg(
                description,
                buildMessageChain {
                    +image
                    appendLine()

                    if(isDetailed) {
                        +"""
                            服务器IP: $ip
                            解析IP: ${ping.host.address.hostName}:${ping.host.port}
                            服务器版本: ${response.version.name}
                            在线人数: ${response.players.online}/${response.players.max}
                            协议版本: ${response.version.protocol}
                            延迟: ${ping.ping}ms
                            
                            MOTD:
                            ${response.description.text}
                        """.trimIndent()
                    } else {
                        +"""
                            服务器IP: $ip
                            服务器版本: ${response.version.name}
                            在线人数: ${response.players.online}/${response.players.max}
                            延迟: ${ping.ping}ms
                        """.trimIndent()
                    }

                    if(isPlayerSample) {
                        appendLine().appendLine()
                        +"> 在线玩家列表"
                        appendLine()

                        +response.players.sample.joinToString {
                            it.name
                        }
                    }
                }
            )
            return@withContext true
        }
    }
    
    suspend fun Contact.pingServerBedrockEdition(
        ip: String,
        isDetailed: Boolean = false
    ): Boolean {
        val (hostname, port) = ip.toIpAndPort() ?: kotlin.run {
            sendMsg(description, "服务器IP格式错误")
            return false
        }
        
        return withContext(Dispatchers.IO) {
            val bean = pingBEServer(ip) ?: run {
                sendMsg(description, "无法连接到服务器")
                return@withContext false
            }
            if(bean.status == "online") {
                if(isDetailed) {
                    sendMsg(description, """
                    服务器IP: $ip
                    服务器版本: ${bean.version}
                    在线人数: ${bean.online}/${bean.max}
                    游戏模式: ${bean.gamemode}
                    协议版本: ${bean.agreement}
                    延迟: ${bean.delay}ms
                    
                    MOTD:
                    ${bean.motd}
                """.trimIndent())
                } else {
                    sendMsg(description, """
                    服务器IP: $ip
                    服务器版本: ${bean.version}
                    在线人数: ${bean.online}/${bean.max}
                    延迟: ${bean.delay}ms
                """.trimIndent())
                }
                true
            } else {
                sendMsg(description, "无法连接到服务器")
                false
            }
        }
    }
    
    
    suspend fun checkServer(ip: String): ServerType {
        val (hostname, port) = ip.toIpAndPort() ?: return NOT

        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val address = InetAddress.getByName(hostname)
                ServerPing(InetSocketAddress(address, port)).fetchData()
            }.onSuccess { 
                return@withContext JAVA_EDITION
            }

            kotlin.runCatching {
                pingBEServer(ip)
            }.onSuccess { 
                if(it?.status == "online")
                    return@withContext BEDROCK_EDITION
            }
            
            NOT
        }
    }
    
    fun pingBEServer(serverIp: String): SBBEServerPing? {
        val (ip, port) = serverIp.toIpAndPort() ?: return null
        return SRequest(apiBEServer).resultBean(mapOf("ip" to ip, "port" to port))
    }
    
    
    private fun String.toIpAndPort(): Pair<String, Int>? {
        val args = split(":")
        if(args.isEmpty() || args.size > 2)
            return null

        val ip = args[0]
        var port = 25565
        if(args.size == 2)
            port = args[1].toInt()
        
        return ip to port
    }
}