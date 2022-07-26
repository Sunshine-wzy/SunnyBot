package io.github.sunshinewzy.sunnybot.objects

import io.github.sunshinewzy.sunnybot.removeColor
import net.kronos.rkon.core.Rcon
import java.io.IOException

class CustomRcon(
    private val host: String,
    private val port: Int,
    private val password: ByteArray
) : Rcon(host, port, password) {
    constructor(host: String, port: Int, password: String) : this(host, port, password.toByteArray())
    
    @Throws(IOException::class)
    override fun command(payload: String): String {
        if (!empty(payload)) {
            try {
                val response = super.command(payload)
                if (empty(response)) {
                    return "指令执行成功"
                }
                return response.removeColor()
            } catch (_: Exception) {
                connect(host, port, password)
                val response = super.command(payload)
                if (empty(response)) {
                    return "指令执行成功"
                }
                return response.removeColor()
            }
        }
        return "指令执行失败"
    }

    override fun toString(): String {
        val s = socket
        return s.inetAddress.toString() + ":" + s.port
    }

    companion object {
        //Short-circuit evaluation
        private fun empty(s: String?): Boolean =
            (s == null) || s.trim { it <= ' ' }.isEmpty()
    }
}