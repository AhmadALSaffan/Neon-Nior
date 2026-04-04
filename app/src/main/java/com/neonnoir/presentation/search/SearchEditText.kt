package com.neonnoir.presentation.search

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText

/**
 * EditText that permanently suppresses the IME extract-mode overlay.
 * The overlay (a black full-screen dialog Android shows when the keyboard
 * thinks it needs extra space) cannot be reliably disabled via XML flags or
 * setImeOptions() alone — different keyboard apps re-evaluate these flags
 * every time they connect. Overriding onCreateInputConnection() is the only
 * guaranteed interception point that runs on every IME connection.
 */
class SearchEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val connection = super.onCreateInputConnection(outAttrs)
        outAttrs.imeOptions = outAttrs.imeOptions or
            EditorInfo.IME_FLAG_NO_EXTRACT_UI or
            EditorInfo.IME_FLAG_NO_FULLSCREEN
        return connection
    }
}
