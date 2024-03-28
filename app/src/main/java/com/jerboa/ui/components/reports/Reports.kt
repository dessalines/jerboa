package com.jerboa.ui.components.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jerboa.R
import com.jerboa.UnreadOrAll
import com.jerboa.datatypes.getLocalizedUnreadOrAllName
import com.jerboa.ui.components.common.MyMarkdownText
import com.jerboa.ui.components.common.UnreadOrAllOptionsDropDown
import com.jerboa.ui.components.person.PersonProfileLink
import com.jerboa.ui.theme.MEDIUM_PADDING
import it.vercruysse.lemmyapi.v0x19.datatypes.Person
import it.vercruysse.lemmyapi.v0x19.datatypes.PersonId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsHeader(
    openDrawer: () -> Unit,
    selectedUnreadOrAll: UnreadOrAll,
    onClickUnreadOrAll: (UnreadOrAll) -> Unit,
    unreadCount: Long? = null,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    var showUnreadOrAllOptions by remember { mutableStateOf(false) }

    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            ReportsHeaderTitle(
                unreadCount = unreadCount,
                selectedUnreadOrAll = selectedUnreadOrAll,
            )
        },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(
                    Icons.Outlined.Menu,
                    contentDescription = stringResource(R.string.home_menu),
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = {
                    showUnreadOrAllOptions = !showUnreadOrAllOptions
                }) {
                    Icon(
                        Icons.Outlined.FilterList,
                        contentDescription = stringResource(R.string.inbox_filter),
                    )
                }

                UnreadOrAllOptionsDropDown(
                    expanded = showUnreadOrAllOptions,
                    selectedUnreadOrAll = selectedUnreadOrAll,
                    onDismissRequest = { showUnreadOrAllOptions = false },
                    onClickUnreadOrAll = {
                        showUnreadOrAllOptions = false
                        onClickUnreadOrAll(it)
                    },
                )
            }
        },
    )
}

@Composable
fun ReportsHeaderTitle(
    selectedUnreadOrAll: UnreadOrAll,
    unreadCount: Long? = null,
) {
    var title = stringResource(R.string.reports)
    val ctx = LocalContext.current
    if (unreadCount != null && unreadCount > 0) {
        title = "$title ($unreadCount)"
    }
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = getLocalizedUnreadOrAllName(ctx, selectedUnreadOrAll),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun ReportCreatorBlock(
    reportCreator: Person,
    onPersonClick: (PersonId) -> Unit,
    showAvatar: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.spacedBy(MEDIUM_PADDING),
    ) {
        Text(
            text = stringResource(R.string.reporter) + ": ",
            style = MaterialTheme.typography.labelLarge,
        )
        PersonProfileLink(
            person = reportCreator,
            onClick = onPersonClick,
            showAvatar = showAvatar,
        )
    }
}

@Composable
fun ReportReasonBlock(reason: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.spacedBy(MEDIUM_PADDING),
    ) {
        Text(
            text = stringResource(R.string.reason) + ": ",
            style = MaterialTheme.typography.labelLarge,
        )

        MyMarkdownText(
            markdown = reason,
            onClick = {},
        )
    }
}

@Composable
fun ReportResolverBlock(
    resolver: Person,
    resolved: Boolean,
    onPersonClick: (PersonId) -> Unit,
    showAvatar: Boolean,
) {
    val resolvedStr = stringResource(if (resolved) R.string.resolved_by else R.string.unresolved_by)

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$resolvedStr: ",
            style = MaterialTheme.typography.labelLarge,
        )
        PersonProfileLink(
            person = resolver,
            onClick = onPersonClick,
            showAvatar = showAvatar,
        )
    }
}

@Composable
fun ResolveButtonBlock(
    resolved: Boolean,
    onResolveClick: () -> Unit,
) {
    TextButton(
        onClick = onResolveClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (resolved) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        ),
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
fun ResolveButtonPreview() {
    ResolveButtonBlock(
        resolved = false,
        onResolveClick = {},
    )
}

@Preview
@Composable
fun UnResolveButtonPreview() {
    ResolveButtonBlock(
        resolved = true,
        onResolveClick = {},
    )
}
