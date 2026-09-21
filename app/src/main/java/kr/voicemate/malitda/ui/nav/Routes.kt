package kr.voicemate.malitda.ui.nav

/** 화면 계약(screen-contract.json) S01~S27과 1:1 대응하는 라우트. */
object Routes {
    const val SPLASH = "splash"            // S01
    const val INTRO = "intro"              // S02
    const val CONSENT = "consent"          // S03
    const val ACCESS = "access"            // S04  ?onboarding=true|false
    const val REGISTER_INTRO = "register"  // S05
    const val TEMPLATE = "template"        // S06
    const val EDIT = "edit"                // S07  ?id=&cat=&label=&text=
    const val HOME = "home"                // S08
    const val LISTENING = "listening"      // S09
    const val RESULT = "result"            // S10
    const val CONFIRM = "confirm"          // S11
    const val APPROVE = "approve"          // S12(잠김) / S21(승인 완료)
    const val EXPRESSIONS = "expressions"  // S13 (+S25 빈 목록)
    const val DETAIL = "detail"            // S14 (+S27 삭제 확인)  /{id}
    const val CORRECTIONS = "corrections"  // S15 (+S22 초기화 확인)
    const val MIC_DENIED = "micDenied"     // S16
    const val NO_RESULT = "noResult"       // S17
    const val SETTINGS = "settings"        // S18
    const val REWARD = "reward"            // S19
    const val NEW_FRIEND = "newFriend"     // S20 /{id}
    const val STT_ERROR = "sttError"       // S23
    const val TTS_ERROR = "ttsError"       // S24
    const val AFTER_SHARE = "afterShare"   // S26

    fun access(onboarding: Boolean) = "$ACCESS?onboarding=$onboarding"
    fun edit(id: Long? = null, cat: String? = null, label: String? = null, text: String? = null): String {
        val q = buildList {
            if (id != null) add("id=$id")
            if (cat != null) add("cat=${enc(cat)}")
            if (label != null) add("label=${enc(label)}")
            if (text != null) add("text=${enc(text)}")
        }
        return if (q.isEmpty()) EDIT else "$EDIT?" + q.joinToString("&")
    }
    fun expressions(cat: String? = null) = if (cat == null) EXPRESSIONS else "$EXPRESSIONS?cat=$cat"
    fun detail(id: Long) = "$DETAIL/$id"
    fun newFriend(id: String) = "$NEW_FRIEND/$id"

    private fun enc(s: String) = java.net.URLEncoder.encode(s, "UTF-8")
}
