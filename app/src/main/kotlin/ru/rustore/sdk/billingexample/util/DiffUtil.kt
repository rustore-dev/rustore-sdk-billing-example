package ru.rustore.sdk.billingexample.util

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

fun <T : Any> createDiffUtil(
    isSame: (oldItem: T, newItem: T) -> Boolean,
): DiffUtil.ItemCallback<T> {
    return object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = isSame(oldItem, newItem)

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
    }
}
