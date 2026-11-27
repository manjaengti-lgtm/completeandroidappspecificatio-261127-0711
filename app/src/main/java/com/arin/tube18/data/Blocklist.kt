package com.arin.tube18.data

object Blocklist {
    private const val SEP = """[\s_\-]*"""

    private val fragments = listOf(
        """teen(?:s|age|aged|ager|agers|ie|ies|y)?""",
        """pre${SEP}teens?""",
        """under${SEP}aged?""",
        """minors?""",
        """child(?:ren)?""",
        """kids?""",
        """lol(?:i|ita|itas|is)""",
        """shota""",
        """jail${SEP}bait""",
        """school${SEP}(?:girl|boy)s?""",
        """young${SEP}(?:girl|boy)s?""",
        """little${SEP}(?:girl|boy)s?""",
        """barely${SEP}legal""",
        """high${SEP}school(?:er)?s?""",
        """juvenile""",
        """(?:pre)?pubescent""",
        """infants?""",
        """toddlers?""",
        """1[0-7]${SEP}(?:yo|y/o|years?${SEP}old)"""
    )

    private val pattern = Regex(
        fragments.joinToString(separator = "|", prefix = """\b(?:""", postfix = """)\b"""),
        RegexOption.IGNORE_CASE
    )

    fun isBlocked(text: String?): Boolean = !text.isNullOrBlank() && pattern.containsMatchIn(text)
}
