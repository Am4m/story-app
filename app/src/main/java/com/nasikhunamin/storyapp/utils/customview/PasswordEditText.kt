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

class PasswordEditText : AppCompatEditText {
    private lateinit var bgEditText: Drawable
    private lateinit var icPassword: Drawable
    private var hintText: String = ""

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        background = bgEditText
        setButtonDrawables(startOfTheText = icPassword)
        compoundDrawablePadding = 40
        hint = hintText
    }

    private fun init() {
        bgEditText = ContextCompat.getDrawable(context, R.drawable.bg_edit_text) as Drawable
        icPassword = ContextCompat.getDrawable(context, R.drawable.ic_baseline_lock_24) as Drawable
        hintText = context.getString(R.string.password)
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length < 8) {
                    setError(context.getString(R.string.password_error), null)
                } else {
                    error = null
                }
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
}