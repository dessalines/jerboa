package com.jerboa.ui.components.person

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jerboa.R
import com.jerboa.datatypes.data
import com.jerboa.datatypes.samplePersonView
import com.jerboa.feat.openMatrix
import com.jerboa.ui.components.common.DotSpacer
import com.jerboa.ui.components.common.LargerCircularIcon
import com.jerboa.ui.components.common.MenuItem
import com.jerboa.ui.components.common.MyMarkdownText
import com.jerboa.ui.components.common.PictrsBannerImage
import com.jerboa.ui.components.common.SortOptionsDropdown
import com.jerboa.ui.components.common.TimeAgo
import com.jerboa.ui.theme.MARKDOWN_BAR_ICON_SIZE
import com.jerboa.ui.theme.MEDIUM_PADDING
import com.jerboa.ui.theme.PROFILE_BANNER_SIZE
import com.jerboa.ui.theme.muted
import it.vercruysse.lemmyapi.dto.SortType
import it.vercruysse.lemmyapi.v0x19.datatypes.PersonView

@Composable
fun PersonProfileTopSection(
    personView: PersonView,
    modifier: Modifier = Modifier,
    showAvatar: Boolean,
    openImageViewer: (url: String) -> Unit,
) {
    Column {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomStart,
        ) {
            personView.person.banner?.also {
                PictrsBannerImage(
                    url = it,
                    contentDescription = stringResource(R.string.personProfile_viewBanner),
                    modifier =
                        Modifier
                            .height(PROFILE_BANNER_SIZE)
                            .clickable {
                                openImageViewer(it)
                            },
                )
            }
            Box(modifier = Modifier.padding(MEDIUM_PADDING)) {
                if (showAvatar) {
                    personView.person.avatar?.also {
                        LargerCircularIcon(
                            icon = it,
                            contentDescription = stringResource(R.string.personProfile_viewAvatar),
                            modifier =
                                Modifier.clickable {
                                    openImageViewer(it)
                                },
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier.padding(MEDIUM_PADDING),
            verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
        ) {
            PersonName(
                person = personView.person,
                style = MaterialTheme.typography.titleMedium,
            )

            TimeAgo(
                precedingString = stringResource(R.string.person_profile_joined),
                longTimeFormat = true,
                published = personView.person.published,
            )
            CommentsAndPosts(personView)
            personView.person.bio?.also {
                MyMarkdownText(
                    markdown = it,
                    color = MaterialTheme.colorScheme.onBackground.muted,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
fun CommentsAndPosts(personView: PersonView) {
    Row {
        Text(
            text = stringResource(R.string.person_profile_posts, personView.counts.post_count),
            color = MaterialTheme.colorScheme.onBackground.muted,
        )
        DotSpacer(style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.person_profile_comments, personView.counts.comment_count),
            color = MaterialTheme.colorScheme.onBackground.muted,
        )
    }
}

@Preview
@Composable
fun CommentsAndPostsPreview() {
    CommentsAndPosts(personView = samplePersonView)
}

@Preview
@Composable
fun PersonProfileTopSectionPreview() {
    PersonProfileTopSection(
        personView = samplePersonView,
        showAvatar = true,
        openImageViewer = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonProfileHeader(
    personName: String,
    myProfile: Boolean,
    banned: Boolean,
    canBan: Boolean,
    onClickSortType: (SortType) -> Unit,
    onBlockPersonClick: () -> Unit,
    onReportPersonClick: () -> Unit,
    onMessagePersonClick: () -> Unit,
    onBanPersonClick: () -> Unit,
    selectedSortType: SortType,
    openDrawer: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: (() -> Unit)? = null,
    isLoggedIn: () -> Boolean,
    matrixId: String?,
) {
    val ctx = LocalContext.current

    var showSortOptions by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }

    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            PersonProfileHeaderTitle(
                personName = personName,
                selectedSortType = selectedSortType,
            )
        },
        navigationIcon = {
            if (onBack == null) {
                IconButton(onClick = openDrawer) {
                    Icon(
                        Icons.Outlined.Menu,
                        contentDescription = stringResource(R.string.home_menu),
                    )
                }
            } else {
                IconButton(onClick = onBack, modifier = Modifier.testTag("jerboa:back")) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.topAppBar_back),
                    )
                }
            }
        },
        actions = {
            Box {
                IconButton(onClick = {
                    showSortOptions = !showSortOptions
                }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = stringResource(R.string.community_sortBy),
                    )
                }

                SortOptionsDropdown(
                    expanded = showSortOptions,
                    onDismissRequest = { showSortOptions = false },
                    onClickSortType = {
                        showSortOptions = false
                        onClickSortType(it)
                    },
                    selectedSortType = selectedSortType,
                )
            }

            if (!myProfile && isLoggedIn()) {
                Box {
                    IconButton(onClick = {
                        showMoreOptions = !showMoreOptions
                    }) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.moreOptions),
                        )
                    }
                    PersonProfileMoreDropdown(
                        personName = personName,
                        expanded = showMoreOptions,
                        banned = banned,
                        canBan = canBan,
                        onDismissRequest = { showMoreOptions = false },
                        onBlockPersonClick = {
                            showMoreOptions = false
                            onBlockPersonClick()
                        },
                        onReportPersonClick = {
                            showMoreOptions = false
                            onReportPersonClick()
                        },
                        onMessagePersonClick = {
                            showMoreOptions = false
                            onMessagePersonClick()
                        },
                        onBanPersonClick = {
                            showMoreOptions = false
                            onBanPersonClick()
                        },
                        openMatrix =
                            matrixId?.let {
                                {
                                    openMatrix(matrixId, ctx)
                                }
                            },
                    )
                }
            }
        },
    )
}

@Composable
fun PersonProfileHeaderTitle(
    personName: String,
    selectedSortType: SortType,
) {
    Column {
        Text(
            text = personName,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = LocalContext.current.getString(selectedSortType.data.shortForm),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun PersonProfileMoreDropdown(
    personName: String,
    banned: Boolean,
    canBan: Boolean,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onBlockPersonClick: () -> Unit,
    onReportPersonClick: () -> Unit,
    onMessagePersonClick: () -> Unit,
    onBanPersonClick: () -> Unit,
    openMatrix: (() -> Unit)?,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        MenuItem(
            text = stringResource(R.string.person_profile_dm_person),
            onClick = onMessagePersonClick,
            icon = Icons.AutoMirrored.Outlined.Message,
        )

        if (openMatrix != null) {
            MenuItem(
                text = stringResource(R.string.matrix_send_msg),
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.matrix_favicon),
                        contentDescription = null,
                        modifier = Modifier.size(MARKDOWN_BAR_ICON_SIZE),
                    )
                },
                onClick = openMatrix,
            )
        }

        HorizontalDivider()
        MenuItem(
            text = stringResource(R.string.block_person, personName),
            onClick = onBlockPersonClick,
            icon = Icons.Outlined.Block,
        )
        MenuItem(
            text = stringResource(R.string.report_person, personName),
            onClick = onReportPersonClick,
            icon = Icons.Outlined.Flag,
        )
        if (canBan) {
            val (banText, banIcon) =
                if (banned) {
                    Pair(stringResource(R.string.unban_person, personName), Icons.Outlined.Restore)
                } else {
                    Pair(stringResource(R.string.ban_person, personName), Icons.Outlined.Gavel)
                }
            MenuItem(
                text = banText,
                onClick = onBanPersonClick,
                icon = banIcon,
            )
        }
    }
}
