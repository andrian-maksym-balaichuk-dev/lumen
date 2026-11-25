package sk.mpage.lumen.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
public fun <T> LumoraSegmented(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    require(options.size in 2..4) { "A segmented control needs two to four options" }

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth().semantics { collectionInfo = CollectionInfo(1, options.size) },
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size, baseShape = CircleShape),
                modifier =
                    Modifier.semantics {
                        collectionItemInfo = CollectionItemInfo(0, 1, index, 1)
                    },
                label = { Text(text = label(option), maxLines = 2, textAlign = TextAlign.Center) },
            )
        }
    }
}

@LumoraPreviews
@Composable
private fun LumoraSegmentedPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Lumora.spacing.gutterCompact),
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.lg),
        ) {
            var difficulty by remember { mutableStateOf("Medium") }
            LumoraSegmented(
                options = listOf("Any", "Easy", "Medium", "Hard"),
                selected = difficulty,
                onSelect = { difficulty = it },
                label = { it },
            )
            LumoraSegmented(
                options = listOf("Classic", "Marathon"),
                selected = "Classic",
                onSelect = {},
                label = { it },
            )
        }
    }
}
