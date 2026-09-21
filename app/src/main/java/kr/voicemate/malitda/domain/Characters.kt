package kr.voicemate.malitda.domain

import kr.voicemate.malitda.R

/** 말친구 캐릭터(선택 기능). 보상은 승인 횟수 기준이며 핵심 기능과 무관하다. */
data class MalFriend(val id: String, val name: String, val drawable: Int, val unlockAt: Int)

object Friends {
    const val UNLOCK_STEP = 5
    val all: List<MalFriend> = listOf(
        MalFriend("purple", "보리", R.drawable.char_purple, 0),
        MalFriend("robot_mint", "로미", R.drawable.char_robot_mint, 5),
        MalFriend("heart", "하티", R.drawable.char_heart, 10),
        MalFriend("water", "물방울", R.drawable.char_water, 15),
        MalFriend("green", "초록이", R.drawable.char_green, 20),
        MalFriend("yellow", "노랑이", R.drawable.char_yellow, 25),
        MalFriend("robot_pink", "핑키", R.drawable.char_robot_pink, 30),
        MalFriend("cloud", "구름이", R.drawable.char_cloud, 35),
        MalFriend("planet", "행성이", R.drawable.char_planet, 40),
        MalFriend("crystal", "크리스", R.drawable.char_crystal, 45),
    )
    fun byId(id: String?): MalFriend = all.firstOrNull { it.id == id } ?: all.first()
    fun unlockedCount(approvals: Int): Int = all.count { it.unlockAt <= approvals }
    fun nextUnlockIn(approvals: Int): Int? = all.firstOrNull { it.unlockAt > approvals }?.let { it.unlockAt - approvals }
}
