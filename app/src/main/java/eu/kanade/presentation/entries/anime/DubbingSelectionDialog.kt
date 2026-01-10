package eu.kanade.presentation.entries.anime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.presentation.components.TabbedDialogPaddings
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun DubbingSelectionDialog(
    availableDubbings: List<String>,
    currentDubbing: String,
    currentQuality: String,
    onDismissRequest: () -> Unit,
    onConfirm: (dubbing: String, quality: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedDubbing by remember { mutableStateOf(currentDubbing.ifBlank { availableDubbings.firstOrNull() ?: "" }) }
    var selectedQuality by remember { mutableStateOf(currentQuality.ifBlank { "best" }) }

    val qualityOptions = listOf("best", "1080p", "720p", "480p", "360p")

    AdaptiveSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .padding(
                    vertical = TabbedDialogPaddings.Vertical,
                    horizontal = TabbedDialogPaddings.Horizontal,
                )
                .fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp, top = 8.dp),
                text = stringResource(MR.strings.label_dubbing),
                style = MaterialTheme.typography.headlineMedium,
            )

            Text(
                text = "Voice Translation",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .sizeIn(maxHeight = 200.dp),
            ) {
                items(availableDubbings) { dubbing ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDubbing = dubbing }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedDubbing == dubbing,
                            onClick = { selectedDubbing = dubbing },
                        )
                        Text(
                            text = dubbing,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Quality",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .sizeIn(maxHeight = 200.dp),
            ) {
                items(qualityOptions) { quality ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedQuality = quality }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedQuality == quality,
                            onClick = { selectedQuality = quality },
                        )
                        Text(
                            text = if (quality == "best") "Best Available" else quality,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(MR.strings.action_cancel))
                }
                Button(
                    onClick = { onConfirm(selectedDubbing, selectedQuality) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(MR.strings.action_save))
                }
            }
        }
    }
}
