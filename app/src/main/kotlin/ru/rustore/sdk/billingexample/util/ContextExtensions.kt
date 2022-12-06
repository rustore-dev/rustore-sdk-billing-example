package ru.rustore.sdk.billingexample.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import ru.rustore.sdk.billingexample.R

fun Context.showAlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit = {},
) {
    AlertDialog.Builder(this).apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton(context.getString(R.string.billing_common_ok)) { dialog, _ ->
            dialog.dismiss()
        }
        setOnDismissListener { onDismiss.invoke() }
        show()
    }
}
