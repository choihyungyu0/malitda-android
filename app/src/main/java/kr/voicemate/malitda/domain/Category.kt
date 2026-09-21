package kr.voicemate.malitda.domain

enum class Category(val key: String, val label: String) {
    NAME("name", "이름"),
    PLACE("place", "장소"),
    TIME("time", "시간"),
    MESSAGE("message", "메시지"),
    OFTEN("often", "자주 쓰는 말");

    companion object {
        fun fromKey(key: String?): Category = entries.firstOrNull { it.key == key } ?: OFTEN
    }
}

object Limits {
    const val RECOMMENDED_MIN = 10
    const val RECOMMENDED_MAX = 20
    const val MAX_EXPRESSIONS = 50
    const val MAX_TEXT_LENGTH = 200
}

data class Template(val category: Category, val label: String, val text: String)

object Templates {
    val all: List<Template> = listOf(
        Template(Category.NAME, "내 이름", "저는 지우예요."),
        Template(Category.NAME, "선생님 부르기", "선생님, 도와주세요."),
        Template(Category.NAME, "친구 부르기", "민수야, 같이 놀자."),
        Template(Category.NAME, "엄마에게", "엄마, 사랑해요."),
        Template(Category.PLACE, "학교 가는 중", "지금 학교에 가고 있어요."),
        Template(Category.PLACE, "집에 있어요", "오늘은 집에서 쉬고 싶어요."),
        Template(Category.PLACE, "화장실", "화장실에 다녀올게요."),
        Template(Category.PLACE, "교실", "교실에서 기다릴게요."),
        Template(Category.TIME, "잠시 후 연락", "잠시 후 연락드릴게요."),
        Template(Category.TIME, "내일 만나요", "저는 내일 친구를 만날 거예요."),
        Template(Category.TIME, "점심시간", "점심시간에 이야기해요."),
        Template(Category.TIME, "늦어요", "조금 늦을 것 같아요."),
        Template(Category.MESSAGE, "고마워요", "도와줘서 고마워요."),
        Template(Category.MESSAGE, "괜찮아요", "저는 괜찮아요. 걱정하지 마세요."),
        Template(Category.MESSAGE, "다시 말해줘", "다시 한 번 말해 주세요."),
        Template(Category.MESSAGE, "재미있었어요", "오늘은 정말 재미있었어요!"),
        Template(Category.OFTEN, "네", "네, 알겠어요."),
        Template(Category.OFTEN, "아니요", "아니요, 괜찮아요."),
        Template(Category.OFTEN, "잠깐만요", "잠깐만 기다려 주세요."),
        Template(Category.OFTEN, "도와주세요", "도와주세요."),
        Template(Category.OFTEN, "물 주세요", "물 마시고 싶어요."),
        Template(Category.OFTEN, "날씨", "오늘 날씨 정말 좋다!"),
    )

    fun byCategory(c: Category) = all.filter { it.category == c }
}
