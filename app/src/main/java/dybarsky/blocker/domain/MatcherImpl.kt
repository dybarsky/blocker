package dybarsky.blocker.domain

import dybarsky.blocker.common.components.Matcher

/**
 * Matches a phone number against a user-defined mask.
 *
 * Rules:
 * - Formatting characters (whitespace, `-`, `(`, `)`, `.`, `/`) are ignored in both the mask
 *   and the number, so `+49 145 *` equals `+49145*` and `+49 (145) 123-45` equals `+4914512345`.
 * - `*` matches any sequence of characters, including an empty one. It can appear anywhere
 *   (`+43*`, `+38050*4567`, `*`).
 * - Everything else is literal and the whole number must match: a mask without `*`
 *   is an exact match (`+43` does NOT match `+436641234567`; use `+43*`).
 * - A leading `00` (after stripping formatting) is the international prefix and equals `+`, in both
 *   the mask and the number: `0049 145 *` matches `+49145123` and `+49145*` matches `0049145123`.
 *   Only the leading `00` is replaced (`+4900…` is untouched). National format (`0151…`) is not mapped.
 * - A mask or number that is empty after normalization never matches.
 */
class MatcherImpl : Matcher {

    override fun invoke(mask: String, text: String): Boolean {
        val pattern = normalize(mask)
        val number = normalize(text)
        if (pattern.isEmpty() || number.isEmpty()) return false
        return matches(pattern, number)
    }

    private fun normalize(value: String): String {
        val stripped = value.filterNot { it in IGNORED || it.isWhitespace() }
        return if (stripped.startsWith(INTERNATIONAL_PREFIX)) "+" + stripped.substring(INTERNATIONAL_PREFIX.length) else stripped
    }

    /** Iterative glob matching with single-point backtracking: O(pattern * number) worst case. */
    private fun matches(pattern: String, text: String): Boolean {
        var p = 0
        var t = 0
        var star = -1
        var resume = 0
        while (t < text.length) {
            when {
                p < pattern.length && pattern[p] == WILDCARD -> {
                    star = p++
                    resume = t
                }
                p < pattern.length && pattern[p] == text[t] -> {
                    p++
                    t++
                }
                star >= 0 -> {
                    p = star + 1
                    t = ++resume
                }
                else -> return false
            }
        }
        while (p < pattern.length && pattern[p] == WILDCARD) p++
        return p == pattern.length
    }

    private companion object {
        const val WILDCARD = '*'
        const val IGNORED = "-()./"
        const val INTERNATIONAL_PREFIX = "00"
    }
}
