package com.jerboa.ui.components.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.jerboa.R
import com.jerboa.datatypes.sampleCommunity
import com.jerboa.datatypes.sampleCommunityFederated
import com.jerboa.datatypes.sampleCommunityView
import com.jerboa.feat.BlurNSFW
import com.jerboa.feat.needBlur
import com.jerboa.ui.components.common.CircularIcon
import com.jerboa.ui.components.common.ItemAndInstanceTitle
import com.jerboa.ui.theme.DRAWER_ITEM_SPACING
import com.jerboa.ui.theme.ICON_SIZE
import com.jerboa.ui.theme.ICON_THUMBNAIL_SIZE
import com.jerboa.ui.theme.LARGER_ICON_THUMBNAIL_SIZE
import com.jerboa.ui.theme.LARGE_PADDING
import com.jerboa.ui.theme.LINK_ICON_SIZE
import com.jerboa.ui.theme.SMALL_PADDING
import it.vercruysse.lemmyapi.v0x19.datatypes.Community
import it.vercruysse.lemmyapi.v0x19.datatypes.CommunityView

@Composable
fun CommunityName(
    modifier: Modifier = Modifier,
    community: Community,
    color: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    ItemAndInstanceTitle(
        title = community.title,
        actorId = community.actor_id,
        local = community.local,
        modifier = modifier,
        itemColor = color,
        itemStyle = style,
    )
}

@Preview
@Composable
fun CommunityNamePreview() {
    CommunityName(community = sampleCommunity)
}

@Preview
@Composable
fun CommunityFederatedNamePreview() {
    CommunityName(community = sampleCommunityFederated)
}

@Composable
fun CommunityLink(
    modifier: Modifier = Modifier,
    community: Community,
    usersPerMonth: Long? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    spacing: Dp = SMALL_PADDING,
    size: Dp = ICON_SIZE,
    thumbnailSize: Int = ICON_THUMBNAIL_SIZE,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    onClick: (community: Community) -> Unit,
    clickable: Boolean = true,
    showDefaultIcon: Boolean,
    showAvatar: Boolean,
    blurNSFW: BlurNSFW,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier =
            if (clickable) {
                modifier.clickable { onClick(community) }
            } else {
                modifier
            },
    ) {
        if (showAvatar) {
            community.icon?.let {
                CircularIcon(
                    icon = it,
                    contentDescription = null,
                    size = size,
                    thumbnailSize = thumbnailSize,
                    blur = blurNSFW.needBlur(community.nsfw),
                )
            } ?: run {
                if (showDefaultIcon) {
                    Icon(
                        imageVector = Icons.Outlined.Forum,
                        contentDescription = "",
                        modifier = Modifier.size(size),
                    )
                }
            }
        }
        Column {
            CommunityName(community = community, color = color, style = style)
            usersPerMonth?.also {
                Text(
                    text = stringResource(R.string.community_link_users_month, usersPerMonth),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun CommunityLinkLarger(
    community: Community,
    onClick: (community: Community) -> Unit,
    showDefaultIcon: Boolean,
    showAvatar: Boolean,
    blurNSFW: BlurNSFW,
) {
    CommunityLink(
        community = community,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleMedium,
        size = LINK_ICON_SIZE,
        thumbnailSize = LARGER_ICON_THUMBNAIL_SIZE,
        spacing = DRAWER_ITEM_SPACING,
        modifier =
            Modifier
                .padding(LARGE_PADDING)
                .fillMaxWidth(),
        onClick = onClick,
        showDefaultIcon = showDefaultIcon,
        blurNSFW = blurNSFW,
        showAvatar = showAvatar,
    )
}

@Composable
fun CommunityLinkLargerWithUserCount(
    communityView: CommunityView,
    onClick: (community: Community) -> Unit,
    showDefaultIcon: Boolean,
    showAvatar: Boolean,
    blurNSFW: BlurNSFW,
) {
    CommunityLink(
        community = communityView.community,
        usersPerMonth = communityView.counts.users_active_month,
        color = MaterialTheme.colorScheme.onSurface,
        size = LINK_ICON_SIZE,
        thumbnailSize = LARGER_ICON_THUMBNAIL_SIZE,
        spacing = DRAWER_ITEM_SPACING,
        modifier =
            Modifier
                .padding(LARGE_PADDING)
                .fillMaxWidth(),
        style = MaterialTheme.typography.titleMedium,
        onClick = onClick,
        showDefaultIcon = showDefaultIcon,
        blurNSFW = blurNSFW,
        showAvatar = showAvatar,
    )
}

@Preview
@Composable
fun CommunityLinkPreview() {
    CommunityLink(
        community = sampleCommunity,
        onClick = {},
        showDefaultIcon = true,
        showAvatar = true,
        blurNSFW = BlurNSFW.NSFW,
    )
}

@Preview
@Composable
fun CommunityLinkWithUsersPreview() {
    CommunityLinkLargerWithUserCount(
        communityView = sampleCommunityView,
        onClick = {},
        showDefaultIcon = true,
        showAvatar = true,
        blurNSFW = BlurNSFW.NSFW,
    )
}
