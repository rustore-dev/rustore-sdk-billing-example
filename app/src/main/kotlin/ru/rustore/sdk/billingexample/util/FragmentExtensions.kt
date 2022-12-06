package ru.rustore.sdk.billingexample.util

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.showToast(message: String, lengthLong: Boolean = false) {
    Toast.makeText(
        requireContext(),
        message,
        if (lengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    ).show()
}
