/*
 * Copyright (c) 2017 Mundo Reader S.L.
 * All Rights Reserved.
 * Confidential and Proprietary - Mundo Reader S.L.
 */

package frangsierra.kotlinfirechat.util

import android.content.DialogInterface
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.view.View


/**
 * Generate a custom dialog with the given values. It works with resources ids or strings.
 * @param title value for the dialog title.
 * @param titleId resource id for the dialog title.
 * @param content value for the dialog content.
 * @param contentId resource id for the dialog content.
 * @param view custom view to be added to the dialog.
 * @param positiveButton value for the dialog positive button text.
 * @param positiveButtonId resource id for the dialog positive button text.
 * @param positiveFunction function invoked when the user click in the positive button.
 * @param negativeButton value for the dialog negative button text.
 * @param negativeButtonId resource id for the dialog negative button text.
 * @param negativeFunction function invoked when the user click in the negative button.
 *
 * @throws IllegalArgumentException if and resource id and a String for the same value are used.
 */
@Suppress("LongParameterList")
fun AlertDialog.Builder.createCustomAlertDialog(@StringRes titleId: Int? = null, title: String? = null,
                                                @StringRes contentId: Int? = null, content: String? = null,
                                                @StringRes positiveButtonId: Int? = null, positiveButton: String? = null,
                                                @StringRes negativeButtonId: Int? = null, negativeButton: String? = null,
                                                @StringRes neutralButtonId: Int? = null, neutralButton: String? = null,
                                                view: View? = null,
                                                positiveFunction: (dialog: DialogInterface?) -> Unit = {},
                                                negativeFunction: (dialog: DialogInterface?) -> Unit = { it!!.dismiss() },
                                                neutralFunction: (dialog: DialogInterface?) -> Unit = {},
                                                cancelable: Boolean = true): AlertDialog {

    // Check if there is not duplicated values
    if (titleId != null && title != null)
        throw IllegalArgumentException("Can't use two different sources for title")
    if (contentId != null && content != null)
        throw IllegalArgumentException("Can't use two different sources for content")
    if (positiveButtonId != null && positiveButton != null)
        throw IllegalArgumentException("Can't use two different sources for positive button")
    if (negativeButtonId != null && negativeButton != null)
        throw IllegalArgumentException("Can't use two different sources for negative button")
    if (neutralButtonId != null && neutralButton != null)
        throw IllegalArgumentException("Can't use two different sources for neutral button")
    // Set up the input title
    titleId?.let { setTitle(it) }
    title?.let { setTitle(it) }
    // Set up content
    content?.let { setMessage(it) }
    contentId?.let { setMessage(it) }
    // Set up view
    view?.let { setView(it) }
    // Set up the buttons
    positiveButton?.let { setPositiveButton(it, { dialog, _ -> positiveFunction.invoke(dialog) }) }
    positiveButtonId?.let { setPositiveButton(it, { dialog, _ -> positiveFunction.invoke(dialog) }) }
    negativeButton?.let { setNegativeButton(it) { dialog, _ -> negativeFunction.invoke(dialog) } }
    negativeButtonId?.let { setNegativeButton(it) { dialog, _ -> negativeFunction.invoke(dialog) } }
    neutralButton?.let { setNeutralButton(it) { dialog, _ -> neutralFunction.invoke(dialog) } }
    neutralButtonId?.let { setNeutralButton(it) { dialog, _ -> neutralFunction.invoke(dialog) } }
    setCancelable(cancelable)
    return create()
}