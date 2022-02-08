package io.github.fvrodas.jaml.ui.commons

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter


object DataBindingAdapters {
    @BindingAdapter("imageDrawable")
    @JvmStatic
    fun setImageUri(view: AppCompatImageView, imageDrawable: Bitmap?) {
        view.setImageBitmap(imageDrawable)
    }


    @BindingAdapter("intColor")
    @JvmStatic
    fun setColor(view: AppCompatTextView, color: Int) {
        view.setTextColor(color)
    }
}