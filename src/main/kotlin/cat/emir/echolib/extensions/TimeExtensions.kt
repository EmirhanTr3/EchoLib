package cat.emir.echolib.extensions

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Parses a duration to a readable string.
 * @param short Whether the output should be shortened; `days` to `d`
 * @param separator Separator for the string when there are multiple durations; `1 days, 1 hour`
 * @return [String]
 */
fun Duration.toReadableString(short: Boolean = false, separator: String = " ", allowTicks: Boolean = false): String {
    fun part(value: Long, singular: String, plural: String, shortForm: String) =
        if (value != 0L) {
            if (short) "$value$shortForm" else "$value ${if (value > 1) plural else singular}"
        } else null

    if (allowTicks && (this.toMillis() / 1000) < 1) return part(this.toMillis() / 50, "tick", "ticks", "t")
        ?: if (short) "0s" else "0 second"

    val strings = listOfNotNull(
        part(this.toDays(), "day", "days", "d"),
        part(this.toHoursPart().toLong(), "hour", "hours", "h"),
        part(this.toMinutesPart().toLong(), "minute", "minutes", "m"),
        part(this.toSecondsPart().toLong(), "second", "seconds", "s")
    )

    return if (strings.isEmpty()) (if (short) "0s" else "0 second") else strings.joinToString(separator)
}

/**
 * Parses a string to a duration.
 * @return [Duration] or null
 */
fun String.toDuration(allowTicks: Boolean = false): Duration? {
    val durationUnit = "y|years?|mo|months?|w|weeks?|d|days?|h|hours?|m|minutes?|s|seconds?" +
            if (allowTicks) "|t|ticks?" else ""

    val durationRegex = Regex("(\\d+)\\s*($durationUnit)", RegexOption.IGNORE_CASE)
    val durationValidationRegex = Regex("^(?:\\s*\\d+\\s*($durationUnit)\\s*)+$", RegexOption.IGNORE_CASE)

    if (!durationValidationRegex.matches(this)) return null

    var duration = Duration.ZERO

    for (match in durationRegex.findAll(this)) {
        val number = match.groupValues[1].toLong()
        val unit = match.groupValues[2].lowercase()
        val type = if (unit.startsWith("mo")) "mo" else unit.first().toString()

        duration = when (type) {
            "t" -> duration.plusMillis(number * 50)
            "s" -> duration.plusSeconds(number)
            "m" -> duration.plusMinutes(number)
            "h" -> duration.plusHours(number)
            "d" -> duration.plusDays(number)
            "w" -> duration.plusDays(7 * number)
            "mo" -> duration.plusDays(30 * number)
            "y" -> duration.plusDays(365 * number)
            else -> duration
        }
    }

    return duration
}

/**
 * Returns how long ago the Instant is.
 * @return [String]
 */
fun Instant.getTimeAgo(): String {
    val seconds = ChronoUnit.SECONDS.between(this, Instant.now())

    if (seconds < 60)
        return "$seconds" + if (seconds == 1L) " second ago" else " seconds ago"

    val minutes = seconds / 60
    if (minutes < 60)
        return "$minutes" + if (minutes == 1L) " minute ago" else " minutes ago"

    val hours = minutes / 60
    if (hours < 24)
        return "$hours" + if (hours == 1L) " hour ago" else " hours ago"

    val days = hours / 24
    return "$days" + if (days == 1L) " day ago" else " days ago"
}