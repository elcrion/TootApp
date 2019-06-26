package xyz.gsora.toot

import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.util.Log

/**
 * Created by gsora on 4/10/17.
 *
 * Wrapper class for Html.{from, to}Html.
 */
internal object CoolHtml {

    fun html(s: String): Spanned {
        if (s.length <= 0) {
            Log.i("CoolHtml", s)
            return SpannableString("")
        }
        val old: Spanned

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            old = Html.fromHtml(s, gimmeHtmlModeIfAny()!!)
        } else {
            old = Html.fromHtml(s)
        }

        var text: CharSequence = old
        while (text.length > 0 && text[text.length - 1] == '\n') {
            text = text.subSequence(0, text.length - 1)
        }

        return SpannableString(text)
    }

    private fun gimmeHtmlModeIfAny(): Int? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.FROM_HTML_MODE_LEGACY
        } else 0

    }
}
