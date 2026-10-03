package com.munzenberger.money.desktop.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.invalid_input_error_description
import org.jetbrains.compose.resources.stringResource

/**
 * A Material 3 outlined text field sized for a desktop app used with a mouse and keyboard. It takes the
 * same parameters as [androidx.compose.material3.OutlinedTextField] and looks the same, but is 40dp tall
 * instead of 56dp, with less padding around the text and smaller targets around its icons (see
 * [DesktopOutlinedTextFieldDefaults]).
 *
 * It's built the way `OutlinedTextField` is, from a [BasicTextField] and
 * [OutlinedTextFieldDefaults.DecorationBox], since `OutlinedTextField` doesn't let you change its
 * content padding.
 */
@Composable
fun DesktopOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    // As in OutlinedTextField, the text style's color wins; otherwise the color follows the field's state.
    val textColor = textStyle.color.takeOrElse { colors.textColor(enabled, isError, focused) }

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.textFieldLayout(hasLabel = label != null, isError = isError),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle.merge(TextStyle(color = textColor)),
            cursorBrush = SolidColor(colors.cursorColor(isError)),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            decorationBox = { innerTextField ->
                // The decoration box pads its icons out to the minimum interactive size, so this sets
                // how much room they take up.
                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentSize provides DesktopOutlinedTextFieldDefaults.IconTargetSize
                ) {
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = value,
                        innerTextField = innerTextField,
                        enabled = enabled,
                        singleLine = singleLine,
                        visualTransformation = visualTransformation,
                        interactionSource = interactionSource,
                        isError = isError,
                        label = label,
                        placeholder = placeholder,
                        leadingIcon = leadingIcon,
                        trailingIcon = trailingIcon,
                        prefix = prefix,
                        suffix = suffix,
                        supportingText = supportingText,
                        colors = colors,
                        contentPadding = DesktopOutlinedTextFieldDefaults.ContentPadding,
                        container = {
                            OutlinedTextFieldDefaults.Container(
                                enabled = enabled,
                                isError = isError,
                                interactionSource = interactionSource,
                                colors = colors,
                                shape = shape,
                            )
                        },
                    )
                }
            },
        )
    }
}

/** Sizes used by [DesktopOutlinedTextField], set one Material density step (-4dp) at a time below the defaults. */
object DesktopOutlinedTextFieldDefaults {
    /** Minimum height of the field's outline: a 24dp line of text plus [ContentPadding]. Material's is 56dp. */
    val MinHeight: Dp = 40.dp

    /** Padding between the outline and the text. Material's is 16dp on every side. */
    val ContentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp)

    /** Room given to leading and trailing icons. Material's is the 48dp touch target. */
    val IconTargetSize: Dp = 40.dp
}

/**
 * Room for the label, error semantics, and the minimum size, applied the way OutlinedTextField does
 * internally. The label floats on the top edge of the outline, half above it, so the field needs room
 * above the outline for that half; semantics are merged first so that padding counts as part of the field.
 */
@Composable
private fun Modifier.textFieldLayout(hasLabel: Boolean, isError: Boolean): Modifier {
    val labelHalfHeight = with(LocalDensity.current) { MoneyTheme.typography.bodySmall.lineHeight.toDp() / 2 }
    val errorDescription = stringResource(Res.string.invalid_input_error_description)

    return this
        .then(if (hasLabel) Modifier.semantics(mergeDescendants = true) {}.padding(top = labelHalfHeight) else Modifier)
        .then(if (isError) Modifier.semantics { error(errorDescription) } else Modifier)
        .defaultMinSize(
            minWidth = OutlinedTextFieldDefaults.MinWidth,
            minHeight = DesktopOutlinedTextFieldDefaults.MinHeight,
        )
}

@Preview
@Composable
private fun DesktopOutlinedTextFieldPreview() {
    PreviewThemed {
        Column(
            modifier = Modifier.padding(MoneyTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MoneyTheme.spacing.small),
        ) {
            DesktopOutlinedTextField(
                value = "Checking",
                onValueChange = {},
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            DesktopOutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Type") },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                supportingText = { Text("Required") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            DesktopOutlinedTextField(
                value = "abc",
                onValueChange = {},
                label = { Text("Initial balance") },
                prefix = { Text("$") },
                supportingText = { Text("Enter a valid amount.") },
                isError = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
