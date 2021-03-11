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


data class SBSounds(
    val sounds: List<SBSound>
): SBean

data class SBSound(
    val category: String,
    val checksum: String,
    val contentType: String,
    val created: String,
    val downloads: Int,
    val duration: Int,
    val explicit: Boolean,
    val extension: String,
    val fileLength: Int,
    val id: Int,
    val region: String,
    val screamDetection: String,
    val slug: String,
    val songDescription: String,
    val soundId: String,
    val soundTags: List<SBSoundTag>,
    val title: String,
    val user: String,
    val voting: Int
)

data class SBSoundTag(
    val id: Int,
    val soundTagId: String,
    val tag: SBTag,
    val voting: Int
)

data class SBTag(
    val id: Int,
    val region: String,
    val tagId: String,
    val text: String
)