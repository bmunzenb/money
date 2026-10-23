package com.munzenberger.money.jewel.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.munzenberger.money.jewel.theme.PreviewThemed
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.GroupHeader
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

/**
 * A titled group of related fields of a form, like a group in the IDE's Settings dialog: a [GroupHeader]
 * (the title followed by a separator line) above the fields, which are indented under it.
 */
@Composable
fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GroupHeader(text = title, modifier = Modifier.semantics { heading() })

        Column(
            modifier = Modifier.padding(start = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

/**
 * A row of a form: the field's [label] in a fixed-width column, followed by the field. An [error] shows
 * below the field, in the error color.
 */
@Composable
fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    field: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = verticalAlignment,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = label, modifier = Modifier.width(FormLabelWidth))
            Column(modifier = Modifier.weight(1f)) {
                field()
            }
        }

        if (error != null) {
            Text(
                text = error,
                color = JewelTheme.globalColors.text.error,
                modifier = Modifier
                    .padding(start = FormLabelWidth + 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

private val FormLabelWidth = 140.dp

@Preview
@Composable
private fun FormSectionPreview() {
    PreviewThemed {
        FormSection(title = "Account", modifier = Modifier.padding(24.dp)) {
            FormField(label = "Account name *") {
                TextField(state = rememberTextFieldState("Checking"), modifier = Modifier.fillMaxWidth())
            }
            FormField(label = "Account number", error = "Enter a valid number.") {
                TextField(state = rememberTextFieldState(), modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
