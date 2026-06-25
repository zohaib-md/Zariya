package com.project.zariya.feature.scanner.data.parser

import com.project.zariya.feature.scanner.domain.model.ExtractedMedicine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrescriptionParser @Inject constructor() {

    companion object {
        private val DOSAGE_PATTERN = Regex(
            """(\d+(?:\.\d+)?)\s*(mg|ml|mcg|µg|g|iu|units?|tab(?:let)?s?|cap(?:sule)?s?|drops?|puff(?:s)?|spray(?:s)?)""",
            RegexOption.IGNORE_CASE
        )

        private val FREQUENCY_ABBREVIATIONS = mapOf(
            "od" to "Once daily",
            "o\\.d\\." to "Once daily",
            "bd" to "Twice daily",
            "b\\.d\\." to "Twice daily",
            "bid" to "Twice daily",
            "b\\.i\\.d\\." to "Twice daily",
            "tds" to "Three times daily",
            "t\\.d\\.s\\." to "Three times daily",
            "tid" to "Three times daily",
            "t\\.i\\.d\\." to "Three times daily",
            "qid" to "Four times daily",
            "q\\.i\\.d\\." to "Four times daily",
            "qds" to "Four times daily",
            "prn" to "As needed",
            "p\\.r\\.n\\." to "As needed",
            "sos" to "If needed",
            "s\\.o\\.s\\." to "If needed",
            "hs" to "At bedtime",
            "h\\.s\\." to "At bedtime",
            "ac" to "Before meals",
            "a\\.c\\." to "Before meals",
            "pc" to "After meals",
            "p\\.c\\." to "After meals",
            "stat" to "Immediately"
        )

        private val FREQUENCY_PHRASES = mapOf(
            "once daily" to "Once daily",
            "once a day" to "Once daily",
            "1 time a day" to "Once daily",
            "1 time daily" to "Once daily",
            "twice daily" to "Twice daily",
            "twice a day" to "Twice daily",
            "2 times a day" to "Twice daily",
            "2 times daily" to "Twice daily",
            "three times daily" to "Three times daily",
            "three times a day" to "Three times daily",
            "3 times a day" to "Three times daily",
            "3 times daily" to "Three times daily",
            "four times daily" to "Four times daily",
            "four times a day" to "Four times daily",
            "4 times a day" to "Four times daily",
            "4 times daily" to "Four times daily",
            "every morning" to "Once daily (morning)",
            "every night" to "Once daily (night)",
            "every evening" to "Once daily (evening)",
            "at bedtime" to "At bedtime",
            "before meals" to "Before meals",
            "after meals" to "After meals",
            "as needed" to "As needed",
            "when required" to "As needed",
            "if needed" to "If needed",
            "every 4 hours" to "Every 4 hours",
            "every 6 hours" to "Every 6 hours",
            "every 8 hours" to "Every 8 hours",
            "every 12 hours" to "Every 12 hours"
        )

        private val DURATION_PATTERN = Regex(
            """(?:(?:for|x|×)\s*)?(\d+)\s*(days?|weeks?|months?|d|w|m)\b""",
            RegexOption.IGNORE_CASE
        )

        private val MEDICINE_LINE_PATTERN = Regex(
            """(?:^|\n)\s*(?:\d+[.)]\s*)?(?:(?:Tab|Cap|Syp|Inj|Drops?|Cream|Oint|Susp|Sol|Gel)\b[.\s]*)?([A-Z][a-zA-Z]+(?:\s*[-/]\s*[A-Z][a-zA-Z]+)*(?:\s+(?:forte|plus|sr|xr|xl|cr|er|ds|ls|xt|retard|junior|paediatric))?)\s*""",
            RegexOption.IGNORE_CASE
        )

        private val COMMON_NON_MEDICINE_WORDS = setOf(
            "patient", "name", "date", "doctor", "dr", "hospital", "clinic",
            "address", "phone", "tel", "age", "sex", "gender", "male", "female",
            "diagnosis", "advice", "follow", "review", "next", "visit",
            "prescription", "rx", "sig", "disp", "refill", "refills",
            "take", "apply", "use", "give", "inject", "inhale",
            "tablet", "capsule", "syrup", "injection", "drops", "cream",
            "ointment", "suspension", "solution", "gel", "tab", "cap",
            "with", "food", "water", "milk", "before", "after", "meals",
            "the", "and", "for", "not", "this", "that", "from",
            "morning", "evening", "night", "daily", "weekly", "monthly"
        )

        private val DOSAGE_FORM_PREFIXES = setOf(
            "tab", "cap", "syp", "inj", "drop", "cream", "oint",
            "susp", "sol", "gel", "inh"
        )
    }

    fun parseText(rawText: String): List<ExtractedMedicine> {
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val medicines = mutableListOf<ExtractedMedicine>()

        for (i in lines.indices) {
            val line = lines[i]
            val contextWindow = buildContextWindow(lines, i)

            val medicineName = extractMedicineName(line) ?: continue

            if (isNonMedicineWord(medicineName)) continue

            val dosage = extractDosage(contextWindow)
            val frequency = extractFrequency(contextWindow)
            val duration = extractDuration(contextWindow)

            val confidence = calculateConfidence(
                name = medicineName,
                dosage = dosage,
                frequency = frequency,
                duration = duration,
                originalLine = line
            )

            if (confidence >= 0.2f) {
                medicines.add(
                    ExtractedMedicine(
                        name = medicineName.trim(),
                        dosage = dosage,
                        frequency = frequency,
                        duration = duration,
                        confidence = confidence
                    )
                )
            }
        }

        return medicines.distinctBy { it.name.lowercase() }
    }

    private fun buildContextWindow(lines: List<String>, currentIndex: Int): String {
        val start = currentIndex
        val end = minOf(currentIndex + 2, lines.size - 1)
        return lines.subList(start, end + 1).joinToString(" ")
    }

    private fun extractMedicineName(line: String): String? {
        val matchResult = MEDICINE_LINE_PATTERN.find(line)
        if (matchResult != null) {
            val name = matchResult.groupValues[1].trim()
            if (name.length >= 3) return cleanMedicineName(name)
        }

        val words = line.split("\\s+".toRegex())
        if (words.isEmpty()) return null

        val firstWord = words[0].replace(Regex("""^\d+[.)]\s*"""), "")
        val cleanFirst = firstWord.trim()

        if (cleanFirst.length >= 3 &&
            cleanFirst[0].isUpperCase() &&
            !isNonMedicineWord(cleanFirst) &&
            !isDosageFormPrefix(cleanFirst)
        ) {
            val nameParts = mutableListOf(cleanFirst)
            for (j in 1 until minOf(words.size, 3)) {
                val word = words[j].trim()
                if (DOSAGE_PATTERN.containsMatchIn(word)) break
                if (isFrequencyAbbreviation(word)) break
                if (word.length >= 2 && word[0].isUpperCase() && !isNonMedicineWord(word)) {
                    nameParts.add(word)
                } else {
                    break
                }
            }
            val fullName = nameParts.joinToString(" ")
            if (fullName.length >= 3) return fullName
        }

        return null
    }

    private fun cleanMedicineName(name: String): String {
        return name.replace(Regex("""\s*(mg|ml|mcg|g|\d+)\s*$""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun extractDosage(text: String): String {
        val match = DOSAGE_PATTERN.find(text)
        return match?.value?.trim() ?: ""
    }

    private fun extractFrequency(text: String): String {
        val lowerText = text.lowercase()

        for ((phrase, meaning) in FREQUENCY_PHRASES) {
            if (lowerText.contains(phrase)) return meaning
        }

        for ((abbreviation, meaning) in FREQUENCY_ABBREVIATIONS) {
            val pattern = Regex("""\b$abbreviation\b""", RegexOption.IGNORE_CASE)
            if (pattern.containsMatchIn(text)) return meaning
        }

        val timesPattern = Regex("""(\d+)\s*(?:times?\s*(?:a\s*)?(?:day|daily))""", RegexOption.IGNORE_CASE)
        val timesMatch = timesPattern.find(text)
        if (timesMatch != null) {
            val count = timesMatch.groupValues[1].toIntOrNull()
            if (count != null) {
                return when (count) {
                    1 -> "Once daily"
                    2 -> "Twice daily"
                    3 -> "Three times daily"
                    4 -> "Four times daily"
                    else -> "$count times daily"
                }
            }
        }

        val hourPattern = Regex("""every\s*(\d+)\s*h(?:ou)?rs?""", RegexOption.IGNORE_CASE)
        val hourMatch = hourPattern.find(text)
        if (hourMatch != null) {
            return "Every ${hourMatch.groupValues[1]} hours"
        }

        return ""
    }

    private fun extractDuration(text: String): String {
        val match = DURATION_PATTERN.find(text)
        if (match != null) {
            val number = match.groupValues[1]
            val rawUnit = match.groupValues[2].lowercase()
            val unit = when {
                rawUnit.startsWith("d") -> if (number == "1") "day" else "days"
                rawUnit.startsWith("w") -> if (number == "1") "week" else "weeks"
                rawUnit.startsWith("m") -> if (number == "1") "month" else "months"
                else -> rawUnit
            }
            return "$number $unit"
        }
        return ""
    }

    private fun calculateConfidence(
        name: String,
        dosage: String,
        frequency: String,
        duration: String,
        originalLine: String
    ): Float {
        var score = 0f

        if (name.length >= 3) score += 0.25f
        if (name[0].isUpperCase()) score += 0.1f

        if (dosage.isNotEmpty()) score += 0.25f

        if (frequency.isNotEmpty()) score += 0.2f

        if (duration.isNotEmpty()) score += 0.1f

        val hasDosageForm = DOSAGE_FORM_PREFIXES.any { prefix ->
            originalLine.lowercase().contains(Regex("""\b$prefix\b"""))
        }
        if (hasDosageForm) score += 0.1f

        return score.coerceIn(0f, 1f)
    }

    private fun isNonMedicineWord(word: String): Boolean {
        return COMMON_NON_MEDICINE_WORDS.contains(word.lowercase())
    }

    private fun isDosageFormPrefix(word: String): Boolean {
        return DOSAGE_FORM_PREFIXES.contains(word.lowercase())
    }

    private fun isFrequencyAbbreviation(word: String): Boolean {
        val lower = word.lowercase().replace(".", "")
        return FREQUENCY_ABBREVIATIONS.keys.any { it.replace("\\.", "").replace(".", "") == lower }
    }
}
