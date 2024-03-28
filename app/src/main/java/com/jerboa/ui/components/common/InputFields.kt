package com.jerboa.ui.components.common

import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material.icons.outlined.Subscript
import androidx.compose.material.icons.outlined.Superscript
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getSelectedText
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.isDigitsOnly
import com.jerboa.R
import com.jerboa.api.API
import com.jerboa.appendMarkdownImage
import com.jerboa.db.entity.Account
import com.jerboa.db.entity.isAnon
import com.jerboa.imageInputStreamFromUri
import com.jerboa.ui.theme.MARKDOWN_BAR_ICON_SIZE
import com.jerboa.ui.theme.MEDIUM_PADDING
import com.jerboa.ui.theme.muted
import kotlinx.coroutines.launch

@Composable
fun MarkdownTextField(
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    account: Account,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    focusImmediate: Boolean = true,
    outlined: Boolean = false,
) {
    val focusRequester = remember { FocusRequester() }
    val imageUploading = rememberSaveable { mutableStateOf(false) }
    val launcher = imageUploadLauncher(account, onTextChange, text, imageUploading)

    var showCreateLink by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }

    if (showCreateLink) {
        CreateLinkDialog(
            value = text,
            onDismissRequest = { showCreateLink = false },
            onClickOk = {
                showCreateLink = false
                onTextChange(it)
            },
        )
    }

    if (showPreview) {
        ShowPreviewDialog(
            content = text.text,
            onDismissRequest = { showPreview = false },
        )
    }

    Column(modifier = modifier) {
        if (outlined) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                label = { Text(text = placeholder) },
                modifier = modifier.focusRequester(focusRequester),
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        // autoCorrect = true,
                    ),
            )
        } else {
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text(text = placeholder) },
                modifier = modifier.focusRequester(focusRequester),
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        // autoCorrect = true,
                    ),
                colors =
                    TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
            )
        }

        MarkdownHelperBar(
            imageUploading = imageUploading.value,
            text = text,
            onTextChange = onTextChange,
            onImageClick = {
                launcher.launch("image/*")
            },
            onLinkClick = {
                showCreateLink = true
            },
            onPreviewClick = {
                showPreview = true
            },
        )
    }

    DisposableEffect(Unit) {
        if (focusImmediate) {
            focusRequester.requestFocus()
        }
        onDispose { }
    }
}

@Composable
fun CreateLinkDialog(
    value: TextFieldValue,
    onClickOk: (TextFieldValue) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val initialText = value.getSelectedText().text

    var text by rememberSaveable { mutableStateOf(initialText) }
    var link by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Column(
                modifier =
                    Modifier
                        .padding(MEDIUM_PADDING)
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
            ) {
                Text(
                    text = stringResource(R.string.input_fields_insert_link),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = {
                        Text(text = stringResource(R.string.input_fields_text))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = {
                        Text(text = stringResource(R.string.input_fields_link))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text(
                    text = stringResource(R.string.input_fields_cancel),
                    color = MaterialTheme.colorScheme.onBackground.muted,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val replacement = "[$text]($link)"
                    val out =
                        value.text.replaceRange(
                            value.selection.start,
                            value.selection.end,
                            replacement,
                        )
                    val end = value.selection.start + replacement.length
                    val cursor = TextRange(end, end)
                    onClickOk(TextFieldValue(out, cursor))
                },
            ) {
                Text(
                    text = stringResource(R.string.input_fields_ok),
                )
            }
        },
    )
}

@Composable
fun ShowPreviewDialog(
    content: String,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                MyMarkdownText(
                    markdown = content,
                    onClick = {},
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text(
                    text = stringResource(R.string.input_fields_ok),
                    color = MaterialTheme.colorScheme.onBackground.muted,
                )
            }
        },
    )
}

@Preview
@Composable
fun CreateLinkDialogPreview() {
    CreateLinkDialog(onClickOk = {}, onDismissRequest = {}, value = TextFieldValue(""))
}

@Composable
private fun imageUploadLauncher(
    account: Account,
    onTextChange: (TextFieldValue) -> Unit,
    text: TextFieldValue,
    imageUploading: MutableState<Boolean>,
): ManagedActivityResultLauncher<String, Uri?> {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Some image upload reqs
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            imageUri = uri
            Log.d("jerboa", imageUri.toString())

            uri?.also { cUri ->
                imageUploading.value = true
                val imageIs = imageInputStreamFromUri(ctx, cUri)
                scope.launch {
                    if (!account.isAnon()) {
                        val url = API.uploadPictrsImage(imageIs, ctx)
                        imageUploading.value = false
                        if (url.isNotEmpty()) {
                            onTextChange(TextFieldValue(appendMarkdownImage(text.text, url)))
                        }
                    }
                }
            }
        }
    return launcher
}

fun simpleMarkdownSurround(
    markdownChar: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    surround: Boolean = true,
) {
    val out =
        if (value.selection.start == value.selection.end) {
            var altered = value.text.insert(value.selection.start, markdownChar)
            if (surround) {
                altered = altered.insert(value.selection.start + markdownChar.length, markdownChar)
            }
            val cursor = TextRange(value.selection.start + markdownChar.length)

            TextFieldValue(altered, cursor)
        } else {
            var altered =
                value.text
                    .insert(value.selection.start, markdownChar)
            if (surround) {
                altered =
                    altered
                        .insert(value.selection.end + markdownChar.length, markdownChar)
            }

            // TODO weird glitch when its the last item
            val start = value.selection.start + markdownChar.length
            val end =
                if (surround) {
                    value.selection.end + markdownChar.length
                } else {
                    value.selection.end
                }
            val cursor =
                if (value.selection.end == value.text.length) {
                    TextRange(start)
                } else {
                    TextRange(
                        start,
                        end,
                    )
                }
            TextFieldValue(altered, cursor)
        }

    onValueChange(out)
}

fun simpleMarkdownSurround(
    startText: String,
    endText: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
) {
    val out =
        if (value.selection.start == value.selection.end) {
            val altered =
                value.text
                    .insert(value.selection.start, startText)
                    .insert(value.selection.start + startText.length, endText)

            val cursor = TextRange(value.selection.start + startText.length)

            TextFieldValue(altered, cursor)
        } else {
            val altered =
                value.text
                    .insert(value.selection.start, startText)
                    .insert(value.selection.end + startText.length, endText)

            val start = value.selection.start + startText.length
            val end = value.selection.end + endText.length

            val cursor =
                if (value.selection.end == value.text.length) {
                    TextRange(start)
                } else {
                    TextRange(start, end)
                }

            TextFieldValue(altered, cursor)
        }

    onValueChange(out)
}

@Composable
fun MarkdownHelperBar(
    onPreviewClick: () -> Unit,
    onImageClick: () -> Unit,
    onLinkClick: () -> Unit,
    imageUploading: Boolean,
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        IconButton(
            onClick = onPreviewClick,
        ) {
            Icon(
                imageVector = Icons.Outlined.Preview,
                contentDescription = stringResource(R.string.markdownHelper_preview),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
        IconButton(
            onClick = onLinkClick,
        ) {
            Icon(
                imageVector = Icons.Outlined.Link,
                contentDescription = stringResource(R.string.markdownHelper_insertLink),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
        IconButton(
            onClick = onImageClick,
            enabled = !imageUploading,
        ) {
            if (imageUploading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = stringResource(R.string.markdownHelper_insertImage),
                    tint = MaterialTheme.colorScheme.onBackground.muted,
                )
            }
        }
        IconButton(
            onClick = {
                simpleMarkdownSurround(
                    "**",
                    value = text,
                    onValueChange = onTextChange,
                )
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.FormatBold,
                contentDescription = stringResource(R.string.markdownHelper_formatBold),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
        IconButton(
            onClick = {
                simpleMarkdownSurround(
                    "*",
                    value = text,
                    onValueChange = onTextChange,
                )
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.FormatItalic,
                contentDescription = stringResource(R.string.markdownHelper_formatItalic),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
        IconButton(
            onClick = {
                simpleMarkdownSurround(
                    "> ",
                    value = text,
                    onValueChange = onTextChange,
                    surround = false,
                )
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.FormatQuote,
                contentDescription = stringResource(R.string.markdownHelper_insertQuote),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
        IconButton(
            onClick = {
                simpleMarkdownSurround(
                    "- ",
                    value = text,
                    onValueChange = onTextChange,
                    surround = false,
                )
            },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.FormatListBulleted,
                contentDescription = stringResource(R.string.markdownHelper_insertList),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
        IconButton(
            onClick = {
                simpleMarkdownSurround(
                    startText = "::: spoiler Title\n",
                    endText = "\n:::",
                    value = text,
                    onValueChange = onTextChange,
                )
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.emergency_home_fill0_wght400_grad0_opsz48),
                contentDescription = stringResource(R.string.markdownHelper_insertSpoiler),
                modifier = Modifier.size(MARKDOWN_BAR_ICON_SIZE),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
        IconButton(
            onClick = {
                simpleMarkdownSurround(
                    "# ",
                    value = text,
                    onValueChange = onTextChange,
                    surround = false,
                )
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.Title,
                contentDescription = stringResource(R.string.markdownHelper_insertHeader),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
        IconButton(
            onClick = {
                simpleMarkdownSurround(
                    "`",
                    value = text,
                    onValueChange = onTextChange,
                )
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.Code,
                contentDescription = stringResource(R.string.markdownHelper_insertCode),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
        IconButton(
            onClick = {
                simpleMarkdownSurround(
                    "~~",
                    value = text,
                    onValueChange = onTextChange,
                )
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.FormatStrikethrough,
                contentDescription = stringResource(R.string.markdownHelper_formatStrikethrough),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
        IconButton(
            onClick = {
                simpleMarkdownSurround(
                    "~",
                    value = text,
                    onValueChange = onTextChange,
                )
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.Subscript,
                contentDescription = stringResource(R.string.markdownHelper_formatSubscript),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
        IconButton(
            onClick = {
                simpleMarkdownSurround(
                    "^",
                    value = text,
                    onValueChange = onTextChange,
                )
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.Superscript,
                contentDescription = stringResource(R.string.markdownHelper_formatSuperscript),
                tint = MaterialTheme.colorScheme.onBackground.muted,
            )
        }
    }
}

@Preview
@Composable
fun TextMarkdownBarPreview() {
    MarkdownHelperBar(
        onPreviewClick = {},
        onImageClick = {},
        onLinkClick = {},
        imageUploading = false,
        text = TextFieldValue(),
        onTextChange = {},
    )
}

@Composable
fun MyMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
    onLongClick: ((View) -> Boolean)? = null,
) {
    MarkdownHelper.CreateMarkdownView(
        markdown = markdown,
        color = color,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
    )
}

@Composable
fun CheckboxField(
    label: String,
    checked: Boolean,
    onCheckedChange: (checked: Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
fun ExpiresField(
    value: Long?,
    onIntChange: (Long?) -> Unit,
    isValid: Boolean,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value?.toString() ?: "",
        onValueChange = {
            if (it.isEmpty()) {
                onIntChange(null)
            } else if (it.isDigitsOnly() && it.toInt() > 0) {
                onIntChange(it.toLongOrNull())
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        placeholder = { Text(text = stringResource(R.string.days_until_expiration)) },
        isError = !isValid,
    )
}

fun String.insert(
    index: Int,
    string: String,
): String {
    return this.substring(0, index) + string + this.substring(index, this.length)
}
