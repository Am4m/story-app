package com.nasikhunamin.storyapp.utils.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.nasikhunamin.storyapp.R

class EmailEditText : AppCompatEditText {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    private fun init() {
        // Pindahkan semua setup ke sini
        val bgEditText = ContextCompat.getDrawable(context, R.drawable.bg_edit_text) as Drawable
        val icEmail = ContextCompat.getDrawable(context, R.drawable.ic_baseline_email_24) as Drawable

        background = bgEditText
        setButtonDrawables(startOfTheText = icEmail)
        compoundDrawablePadding = 40
        hint = context.getString(R.string.email)
        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty() && !isValidEmail(s)) error = context.getString(R.string.email_error)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setButtonDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }

    private fun isValidEmail(email: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}