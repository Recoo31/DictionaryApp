package com.reco.ferhengakurdi.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

fun copyToClipboard(context: Context, textToCopy: String, label: String = "Copied Text") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, textToCopy)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(context, "Text copied", Toast.LENGTH_SHORT).show()
}

@Composable
fun AnnotatedString.Builder.ProcessElement(element: Element) {
    when {
        element.tagName() == "strong" -> {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            ) {
                append(element.text())
            }
        }

        else -> {
            element.childNodes().forEach { node ->
                if (node is Element) {
                    ProcessElement(node)
                } else if (node is TextNode) {
                    append(node.text())
                }
            }
        }
    }
}


//https://github.com/wilinz/globalization-translator/blob/56e7d9362a79c9e4b91c415ab911a8a54274e809/src/main/kotlin/com/wilinz/globalization/translator/translator/engine/token.kt#L6
fun getGoogleToken(text: String): String {
    val a = text.toByteArray().map { it.toUByte().toShort() }

    val d = 406644L
    val e = 3293161072L
    var f = 406644L
    for (h in a) {
        f += h
        f = calculate(f, "+-a^+6")
    }
    f = calculate(f, "+-3^+b+-f")
    f = f xor e
    if (0 > f) {
        f = (f and Int.MAX_VALUE.toLong()) + Int.MAX_VALUE.toLong() + 1
    }
    f = (f % 1E6).toLong()

    return "$f.${f xor d}"
}

private fun calculate(a: Long, b: String): Long {
    var g = a
    for (c in 0..b.length - 2 step 3) {
        val d = b[c + 2]
        val e = if ('a' <= d) (d - 87).toInt() else d.toString().toInt()
        val f = if ('+' == b[c + 1]) g.ushr(e) else g shl e
        g = if ('+' == b[c]) g + f and (Int.MAX_VALUE.toLong() * 2 + 1) else g xor f
    }

    return g
}