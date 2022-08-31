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
            sendMsg(description, "������IP��ʽ����")
            return false
        }

        return withContext(Dispatchers.IO) {
            val ping = kotlin.runCatching {
                val address = InetAddress.getByName(hostname)
                ServerPing(InetSocketAddress(address, port))
            }.onFailure {
                sendMsg(description, "�޷����ӵ�������")
            }.getOrNull() ?: return@withContext false
            
            val response = runCatching { 
                ping.fetchData()
            }.onFailure {
                sendMsg(description, "�޷���ȡ������״̬")
            }.getOrNull() ?: return@withContext false

            val image = response.faviconInputStream?.use {
                uploadImage(it)
            } ?: kotlin.run {
                sendMsg(description, "������ͼ���ȡʧ��")
                return@withContext false
            }

            sendMsg(
                description,
                buildMessageChain {
                    +image
                    appendLine()

                    if(isDetailed) {
                        +"""
                            ������IP: $ip
                            ����IP: ${ping.host.address.hostName}:${ping.host.port}
                            �������汾: ${response.version.name}
                            ��������: ${response.players.online}/${response.players.max}
                            Э��汾: ${response.version.protocol}
                            �ӳ�: ${ping.ping}ms
                            
                            MOTD:
                            ${response.description.text}
                        """.trimIndent()
                    } else {
                        +"""
                            ������IP: $ip
                            �������汾: ${response.version.name}
                            ��������: ${response.players.online}/${response.players.max}
                            �ӳ�: ${ping.ping}ms
                        """.trimIndent()
                    }

                    if(isPlayerSample) {
                        appendLine().appendLine()
                        +"> ��������б�"
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
            sendMsg(description, "������IP��ʽ����")
            return false
        }
        
        return withContext(Dispatchers.IO) {
            val bean = pingBEServer(ip) ?: run {
                sendMsg(description, "�޷����ӵ�������")
                return@withContext false
            }
            if(bean.status == "online") {
                if(isDetailed) {
                    sendMsg(description, """
                    ������IP: $ip
                    �������汾: ${bean.version}
                    ��������: ${bean.online}/${bean.max}
                    ��Ϸģʽ: ${bean.gamemode}
                    Э��汾: ${bean.agreement}
                    �ӳ�: ${bean.delay}ms
                    
                    MOTD:
                    ${bean.motd}
                """.trimIndent())
                } else {
                    sendMsg(description, """
                    ������IP: $ip
                    �������汾: ${bean.version}
                    ��������: ${bean.online}/${bean.max}
                    �ӳ�: ${bean.delay}ms
                """.trimIndent())
                }
                true
            } else {
                sendMsg(description, "�޷����ӵ�������")
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