package kr.voicemate.malitda.ui.figma

import kr.voicemate.malitda.R

/** 화면키 → Figma 목업 이미지 리소스. */
val FIGMA_IMAGE: Map<String, Int> = mapOf(
    "S01" to R.drawable.fig_s01, "S02" to R.drawable.fig_s02, "S03" to R.drawable.fig_s03,
    "S04" to R.drawable.fig_s04, "S05" to R.drawable.fig_s05, "S06" to R.drawable.fig_s06,
    "S07" to R.drawable.fig_s07, "S08" to R.drawable.fig_s08, "S09" to R.drawable.fig_s09,
    "S10" to R.drawable.fig_s10, "S11" to R.drawable.fig_s11, "S12" to R.drawable.fig_s12,
    "S13" to R.drawable.fig_s13, "S14" to R.drawable.fig_s14, "S15" to R.drawable.fig_s15,
    "S16" to R.drawable.fig_s16, "S17" to R.drawable.fig_s17, "S18" to R.drawable.fig_s18,
    "S19" to R.drawable.fig_s19, "S20" to R.drawable.fig_s20, "S21" to R.drawable.fig_s21,
    "S22" to R.drawable.fig_s22, "S23" to R.drawable.fig_s23, "S24" to R.drawable.fig_s24,
    "S25" to R.drawable.fig_s25, "S26" to R.drawable.fig_s26, "S27" to R.drawable.fig_s27,
    "O_MENU" to R.drawable.fig_o_menu, "O_LISTEN" to R.drawable.fig_o_listen,
    "O_PERMISSION" to R.drawable.fig_o_permission, "O_PRIVACY" to R.drawable.fig_o_privacy,
    "O_FONT" to R.drawable.fig_o_font, "O_TTS" to R.drawable.fig_o_tts,
    "O_DELETE_ALL" to R.drawable.fig_o_delete_all, "O_DELETE_CORRECTION" to R.drawable.fig_o_delete_correction,
    "O_CONTACT" to R.drawable.fig_o_contact, "O_CATALOG" to R.drawable.fig_o_catalog,
    "O_REWARD" to R.drawable.fig_o_reward, "O_SEARCH_TEMPLATE" to R.drawable.fig_o_search_template,
    "O_SEARCH_EXPRESSION" to R.drawable.fig_o_search_expression, "O_EDIT_TITLE" to R.drawable.fig_o_edit_title,
    "O_EDIT_EXPRESSION" to R.drawable.fig_o_edit_expression, "O_EDIT_MESSAGE" to R.drawable.fig_o_edit_message,
    "O_INPUT_ERROR" to R.drawable.fig_o_input_error, "O_CATEGORY" to R.drawable.fig_o_category,
    "O_VOICE_REGISTER" to R.drawable.fig_o_voice_register, "O_SHARE" to R.drawable.fig_o_share,
    "O_COPIED" to R.drawable.fig_o_copied, "O_CONSENT" to R.drawable.fig_o_consent,
)

/** 오버레이(팝업)로 뜨는 화면들 — 이전 화면 위에 반투명 배경으로 표시. */
val OVERLAY_KEYS: Set<String> = setOf(
    "O_MENU", "O_LISTEN", "O_PERMISSION", "O_PRIVACY", "O_FONT", "O_TTS",
    "O_DELETE_ALL", "O_DELETE_CORRECTION", "O_CONTACT", "O_CATALOG", "O_REWARD",
    "O_SEARCH_TEMPLATE", "O_SEARCH_EXPRESSION", "O_EDIT_TITLE", "O_EDIT_EXPRESSION",
    "O_EDIT_MESSAGE", "O_INPUT_ERROR", "O_CATEGORY", "O_VOICE_REGISTER", "O_SHARE",
    "O_COPIED", "O_CONSENT",
)
