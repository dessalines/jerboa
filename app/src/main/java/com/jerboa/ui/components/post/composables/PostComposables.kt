package com.jerboa.ui.components.post.composables

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jerboa.InputField
import com.jerboa.R
import com.jerboa.datatypes.sampleCommunity
import com.jerboa.db.entity.Account
import com.jerboa.db.entity.AnonAccount
import com.jerboa.isImage
import com.jerboa.ui.components.common.ApiErrorText
import com.jerboa.ui.components.common.CheckboxField
import com.jerboa.ui.components.common.CircularIcon
import com.jerboa.ui.components.common.MarkdownTextField
import com.jerboa.ui.components.common.PickImage
import com.jerboa.ui.components.common.PictrsUrlImage
import com.jerboa.ui.theme.ICON_SIZE
import com.jerboa.ui.theme.MEDIUM_PADDING
import com.jerboa.ui.theme.THUMBNAIL_SIZE
import com.jerboa.ui.theme.muted
import it.vercruysse.lemmyapi.v0x19.datatypes.Community

@Composable
fun CreateEditPostBody(
    name: String,
    nameField: InputField,
    onNameChange: (name: String) -> Unit,
    body: TextFieldValue,
    onBodyChange: (body: TextFieldValue) -> Unit,
    url: String,
    urlField: InputField,
    onUrlChange: (url: String) -> Unit,
    sharedImage: Uri? = null,
    onImagePicked: (image: Uri) -> Unit,
    isUploadingImage: Boolean = false,
    isNsfw: Boolean,
    onIsNsfwChange: (isNsfw: Boolean) -> Unit,
    suggestedTitle: String? = null,
    suggestedTitleLoading: Boolean = false,
    communitySelector: @Composable () -> Unit,
    account: Account,
    padding: PaddingValues,
    error: Throwable?,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier =
            Modifier
                .verticalScroll(scrollState)
                .imePadding()
                .padding(
                    vertical = padding.calculateTopPadding(),
                    horizontal = MEDIUM_PADDING,
                )
                .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
    ) {
        error?.let {
            ApiErrorText(msg = error)
        }
        /**
         * Post Name / Title TextField
         */
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            isError = nameField.hasError,
            label = {
                Text(text = nameField.label)
            },
            modifier = Modifier.fillMaxWidth(),
        )

        /**
         * Post URL TextField
         */
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = {
                Text(text = urlField.label)
            },
            isError = urlField.hasError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )

        /**
         * Show title suggestion for web links
         */
        if (suggestedTitleLoading) {
            CircularProgressIndicator()
        } else {
            suggestedTitle?.let {
                Text(
                    text = stringResource(R.string.create_post_copy_suggested_title, it),
                    color = MaterialTheme.colorScheme.onBackground.muted,
                    modifier = Modifier.clickable { onNameChange(it) },
                )
            }
        }

        /**
         * Pick and upload an image, show a preview if possible
         */
        PickImage(
            onPickedImage = onImagePicked,
            sharedImage = sharedImage,
            horizontalAlignment = Alignment.End,
            isUploadingImage = isUploadingImage,
        )

        if (isImage(url)) {
            PictrsUrlImage(
                url = url,
                blur = false,
            )
        }

        /**
         * Markdown field for post body
         */
        MarkdownTextField(
            text = body,
            onTextChange = onBodyChange,
            outlined = true,
            account = account,
            focusImmediate = false,
            placeholder = stringResource(R.string.post_edit_body_placeholder),
            modifier = Modifier.fillMaxWidth(),
        )

        /**
         * Show community selector if necessary
         */
        communitySelector()

        /**
         * Checkbox to mark post NSFW
         */
        CheckboxField(label = stringResource(R.string.create_post_tag_nsfw), checked = isNsfw, onCheckedChange = onIsNsfwChange)
    }
}

@Composable
fun PostCommunitySelector(
    community: Community?,
    onClickCommunityList: () -> Unit,
) {
    Box {
        community?.also {
            OutlinedTextField(
                value = community.name,
                readOnly = true,
                onValueChange = {},
                label = {
                    Text(stringResource(R.string.create_post_community))
                },
                leadingIcon = {
                    community.icon?.let {
                        CircularIcon(
                            icon = it,
                            contentDescription = stringResource(R.string.community_icon),
                            size = ICON_SIZE,
                            thumbnailSize = THUMBNAIL_SIZE,
                        )
                    }
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = stringResource(R.string.createPost_selectCommunity),
                    )
                },
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )
        } ?: run {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = {
                    Text(stringResource(R.string.create_post_community))
                },
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )
        }
        // A box to draw over the textview and override clicks
        Box(
            modifier =
                Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .clickable {
                        onClickCommunityList()
                    },
        )
    }
}

@Preview
@Composable
fun CreatePostBodyPreview() {
    CreateEditPostBody(
        name = "",
        nameField = InputField(label = "", hasError = false),
        onNameChange = {},
        body = TextFieldValue(""),
        onBodyChange = {},
        url = "",
        urlField = InputField(label = "", hasError = false),
        onUrlChange = {},
        onImagePicked = {},
        account = AnonAccount,
        padding = PaddingValues(),
        suggestedTitle = null,
        suggestedTitleLoading = false,
        isNsfw = false,
        onIsNsfwChange = {},
        communitySelector = {
            PostCommunitySelector(
                community = sampleCommunity,
                onClickCommunityList = {},
            )
        },
        error = null,
    )
}

@Preview
@Composable
fun CreatePostBodyPreviewNoCommunity() {
    CreateEditPostBody(
        name = "",
        nameField = InputField(label = "", hasError = false),
        onNameChange = {},
        body = TextFieldValue(""),
        onBodyChange = {},
        url = "",
        urlField = InputField(label = "", hasError = false),
        onUrlChange = {},
        onImagePicked = {},
        suggestedTitle = stringResource(R.string.create_post_a_title_here),
        suggestedTitleLoading = false,
        account = AnonAccount,
        padding = PaddingValues(),
        isNsfw = false,
        onIsNsfwChange = {},
        communitySelector = {
            PostCommunitySelector(
                community = null,
                onClickCommunityList = {},
            )
        },
        error = null,
    )
}

@Preview
@Composable
fun EditPostBodyPreview() {
    CreateEditPostBody(
        name = "",
        nameField = InputField(label = "test", hasError = false),
        body = TextFieldValue(""),
        url = "",
        urlField = InputField(label = "url", hasError = false),
        onBodyChange = {},
        onNameChange = {},
        onImagePicked = {},
        onUrlChange = {},
        account = AnonAccount,
        isNsfw = false,
        onIsNsfwChange = {},
        padding = PaddingValues(),
        communitySelector = {},
        error = null,
    )
}
