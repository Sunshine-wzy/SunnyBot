package io.github.sunshinewzy.sunnybot.objects

interface SBean

data class RosellemcServerInfo(
    val code: Int,
    val message: String,
    val res: RSIRes,
    val run_time: Double
): SBean

data class RSIRes(
    val favicon: String,
    val server_player_average: Double,
    val server_player_history_max: Int,
    val server_player_max: Int,
    val server_player_online: Int,
    val server_player_yesterday_average: String,
    val server_player_yesterday_max: Int,
    val server_status: Int,
    val update_time: String
): SBean
