package dybarsky.blocker.domain

import dybarsky.blocker.common.components.Matcher

class MatcherImpl : Matcher {

    override fun invoke(mask: String, text: String): Boolean {
        return text.zip(mask).all { (char, mask) ->
            mask == '*' || char == mask
        }
    }
}
