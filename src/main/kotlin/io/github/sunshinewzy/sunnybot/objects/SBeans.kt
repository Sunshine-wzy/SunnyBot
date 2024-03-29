package io.github.sunshinewzy.sunnybot.objects

interface SBean


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

data class SBBEServerPing(
    val agreement: String,
    val delay: Int,
    val gamemode: String,
    val ip: String,
    val max: String,
    val motd: String,
    val online: String,
    val port: String,
    val status: String,
    val version: String
): SBean


data class SBOwnThink(
    val `data`: SBOwnThinkData,
    val message: String
): SBean

data class SBOwnThinkData(
    val info: SBOwnThinkInfo,
    val type: Int
)

data class SBOwnThinkInfo(
    val text: String
)