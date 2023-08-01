package com.jerboa.ui.components.person

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jerboa.R
import com.jerboa.datatypes.samplePersonView
import com.jerboa.datatypes.types.PersonView
import com.jerboa.datatypes.types.SortType
import com.jerboa.personNameShown
import com.jerboa.ui.components.common.DotSpacer
import com.jerboa.ui.components.common.IconAndTextDrawerItem
import com.jerboa.ui.components.common.LargerCircularIcon
import com.jerboa.ui.components.common.MyMarkdownText
import com.jerboa.ui.components.common.PictrsBannerImage
import com.jerboa.ui.components.common.SortOptionsDialog
import com.jerboa.ui.components.common.SortTopOptionsDialog
import com.jerboa.ui.components.common.TimeAgo
import com.jerboa.ui.theme.MEDIUM_PADDING
import com.jerboa.ui.theme.PROFILE_BANNER_SIZE
import com.jerboa.ui.theme.muted

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
                    modifier = Modifier
                        .height(PROFILE_BANNER_SIZE)
                        .clickable {
                            openImageViewer(personView.person.banner)
                        },
                )
            }
            Box(modifier = Modifier.padding(MEDIUM_PADDING)) {
                if (showAvatar) {
                    personView.person.avatar?.also {
                        LargerCircularIcon(
                            icon = it,
                            contentDescription = stringResource(R.string.personProfile_viewAvatar),
                            modifier = Modifier.clickable {
                                openImageViewer(personView.person.avatar)
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
            Text(
                text = personNameShown(personView.person, true),
                style = MaterialTheme.typography.titleLarge,
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
    onClickSortType: (SortType) -> Unit,
    onBlockPersonClick: () -> Unit,
    onReportPersonClick: () -> Unit,
    onMessagePersonClick: () -> Unit,
    selectedSortType: SortType,
    openDrawer: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: (() -> Unit)? = null,
    isLoggedIn: () -> Boolean,
    siteVersion: String,
) {
    var showSortOptions by remember { mutableStateOf(false) }
    var showTopOptions by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }

    if (showSortOptions) {
        SortOptionsDialog(
            selectedSortType = selectedSortType,
            onDismissRequest = { showSortOptions = false },
            onClickSortType = {
                showSortOptions = false
                onClickSortType(it)
            },
            onClickSortTopOptions = {
                showSortOptions = false
                showTopOptions = !showTopOptions
            },
            siteVersion = siteVersion,
        )
    }

    if (showTopOptions) {
        SortTopOptionsDialog(
            selectedSortType = selectedSortType,
            onDismissRequest = { showTopOptions = false },
            onClickSortType = {
                showTopOptions = false
                onClickSortType(it)
            },
            siteVersion = siteVersion,
        )
    }

    if (showMoreOptions) {
        PersonProfileMoreDialog(
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
        )
    }

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
                        Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.topAppBar_back),
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {
                showSortOptions = !showSortOptions
            }) {
                Icon(
                    Icons.Outlined.Sort,
                    contentDescription = stringResource(R.string.selectSort),
                )
            }
            if (!myProfile && isLoggedIn()) {
                IconButton(onClick = {
                    showMoreOptions = !showMoreOptions
                }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.moreOptions),
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
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = LocalContext.current.getString(selectedSortType.shortForm),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun PersonProfileMoreDialog(
    onDismissRequest: () -> Unit,
    onBlockPersonClick: () -> Unit,
    onReportPersonClick: () -> Unit,
    onMessagePersonClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Column {
                IconAndTextDrawerItem(
                    text = stringResource(R.string.person_profile_dm_person),
                    onClick = onMessagePersonClick,
                    icon = Icons.Outlined.Message,
                )
                IconAndTextDrawerItem(
                    text = stringResource(R.string.person_profile_block_person),
                    icon = Icons.Outlined.Block,
                    onClick = onBlockPersonClick,
                )
                IconAndTextDrawerItem(
                    text = stringResource(R.string.person_profile_report_person),
                    icon = Icons.Outlined.Flag,
                    onClick = onReportPersonClick,
                )
            }
        },
        confirmButton = {},
    )
}
