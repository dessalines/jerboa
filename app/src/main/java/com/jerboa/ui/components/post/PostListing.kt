package com.jerboa.ui.components.post

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CommentsDisabled
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jerboa.JerboaAppState
import com.jerboa.PostType
import com.jerboa.PostViewMode
import com.jerboa.R
import com.jerboa.datatypes.BanFromCommunityData
import com.jerboa.datatypes.PostFeatureData
import com.jerboa.datatypes.VoteDisplayMode
import com.jerboa.datatypes.sampleImagePostView
import com.jerboa.datatypes.sampleLinkNoThumbnailPostView
import com.jerboa.datatypes.sampleLinkPostView
import com.jerboa.datatypes.sampleMarkdownPostView
import com.jerboa.datatypes.samplePostView
import com.jerboa.db.entity.Account
import com.jerboa.db.entity.AnonAccount
import com.jerboa.feat.BlurNSFW
import com.jerboa.feat.InstantScores
import com.jerboa.feat.PostActionBarMode
import com.jerboa.feat.SwipeToActionPreset
import com.jerboa.feat.SwipeToActionType
import com.jerboa.feat.VoteType
import com.jerboa.feat.amMod
import com.jerboa.feat.canMod
import com.jerboa.feat.isReadyAndIfNotShowSimplifiedInfoToast
import com.jerboa.feat.needBlur
import com.jerboa.feat.simulateModerators
import com.jerboa.getPostType
import com.jerboa.hostName
import com.jerboa.isSameInstance
import com.jerboa.nsfwCheck
import com.jerboa.rememberJerboaAppState
import com.jerboa.siFormat
import com.jerboa.toHttps
import com.jerboa.ui.components.common.ActionBarButton
import com.jerboa.ui.components.common.ActionBarButtonAndBadge
import com.jerboa.ui.components.common.CircularIcon
import com.jerboa.ui.components.common.CommentOrPostNodeHeader
import com.jerboa.ui.components.common.DotSpacer
import com.jerboa.ui.components.common.MarkdownHelper.CreateMarkdownPreview
import com.jerboa.ui.components.common.MyMarkdownText
import com.jerboa.ui.components.common.NsfwBadge
import com.jerboa.ui.components.common.PictrsThumbnailImage
import com.jerboa.ui.components.common.PictrsUrlImage
import com.jerboa.ui.components.common.ScoreAndTime
import com.jerboa.ui.components.common.SimpleTopAppBar
import com.jerboa.ui.components.common.SwipeToAction
import com.jerboa.ui.components.common.TimeAgo
import com.jerboa.ui.components.common.VoteGeneric
import com.jerboa.ui.components.common.rememberSwipeActionState
import com.jerboa.ui.components.common.scoreColor
import com.jerboa.ui.components.community.CommunityLink
import com.jerboa.ui.components.community.CommunityName
import com.jerboa.ui.components.person.PersonProfileLink
import com.jerboa.ui.components.post.composables.PostOptionsDropdown
import com.jerboa.ui.theme.ACTION_BAR_ICON_SIZE
import com.jerboa.ui.theme.CARD_COLORS
import com.jerboa.ui.theme.LARGER_ICON_THUMBNAIL_SIZE
import com.jerboa.ui.theme.LARGE_PADDING
import com.jerboa.ui.theme.LINK_ICON_SIZE
import com.jerboa.ui.theme.MEDIUM_ICON_SIZE
import com.jerboa.ui.theme.MEDIUM_PADDING
import com.jerboa.ui.theme.POST_LINK_PIC_SIZE
import com.jerboa.ui.theme.SMALLER_PADDING
import com.jerboa.ui.theme.SMALL_PADDING
import com.jerboa.ui.theme.THUMBNAIL_CARET_SIZE
import com.jerboa.ui.theme.XXL_PADDING
import com.jerboa.ui.theme.jerboaColorScheme
import com.jerboa.ui.theme.muted
import it.vercruysse.lemmyapi.v0x19.datatypes.*
import kotlinx.coroutines.CoroutineScope

@Composable
fun PostHeaderLine(
    postView: PostView,
    instantScores: InstantScores,
    onCommunityClick: (community: Community) -> Unit,
    onPersonClick: (personId: PersonId) -> Unit,
    modifier: Modifier = Modifier,
    showCommunityName: Boolean = true,
    showAvatar: Boolean,
    fullBody: Boolean,
    blurNSFW: BlurNSFW,
    voteDisplayMode: VoteDisplayMode,
) {
    val community = postView.community
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                if (showCommunityName && showAvatar) {
                    community.icon?.let {
                        CircularIcon(
                            icon = it,
                            contentDescription = stringResource(R.string.postListing_goToCommunity),
                            size = MEDIUM_ICON_SIZE,
                            modifier = Modifier.clickable { onCommunityClick(community) },
                            thumbnailSize = LARGER_ICON_THUMBNAIL_SIZE,
                            blur = blurNSFW.needBlur(community.nsfw),
                        )
                    }
                }
                Column {
                    if (showCommunityName) {
                        CommunityName(
                            community = postView.community,
                            modifier = Modifier.clickable { onCommunityClick(community) },
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
                    ) {
                        PersonProfileLink(
                            person = postView.creator,
                            onClick = onPersonClick,
                            showTags = fullBody,
                            // Set this to false, we already know this
                            isPostCreator = false,
                            isCommunityBanned = postView.creator_banned_from_community,
                            color = MaterialTheme.colorScheme.onSurface.muted,
                            showAvatar = !showCommunityName && showAvatar,
                        )
                        if (postView.post.featured_local) {
                            DotSpacer()
                            Icon(
                                imageVector = Icons.Outlined.PushPin,
                                contentDescription = stringResource(R.string.postListing_featuredLocal),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(ACTION_BAR_ICON_SIZE),
                            )
                        }
                        if (postView.post.featured_community) {
                            DotSpacer()
                            Icon(
                                imageVector = Icons.Outlined.PushPin,
                                contentDescription = stringResource(R.string.postListing_featuredCommunity),
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(ACTION_BAR_ICON_SIZE),
                            )
                        }
                        if (postView.post.locked) {
                            DotSpacer()
                            Icon(
                                imageVector = Icons.Outlined.CommentsDisabled,
                                contentDescription = stringResource(R.string.postListing_locked),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(ACTION_BAR_ICON_SIZE),
                            )
                        }
                    }
                }
                if (postView.post.deleted) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.postListing_deleted),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                if (postView.post.removed) {
                    Icon(
                        imageVector = Icons.Outlined.Gavel,
                        contentDescription = stringResource(R.string.removed),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            ScoreAndTime(
                instantScores = instantScores,
                published = postView.post.published,
                updated = postView.post.updated,
                isNsfw = nsfwCheck(postView),
                voteDisplayMode = voteDisplayMode,
            )
        }
    }
}

@Preview
@Composable
fun PostHeaderLinePreview() {
    val postView = sampleLinkPostView
    PostHeaderLine(
        postView = postView,
        instantScores = InstantScores(
            myVote = 0,
            score = 10,
            upvotes = 9,
            downvotes = 1,
        ),
        onCommunityClick = {},
        onPersonClick = {},
        showAvatar = true,
        blurNSFW = BlurNSFW.NSFW,
        fullBody = true,
        voteDisplayMode = VoteDisplayMode.Full,
    )
}

@Composable
fun PostNodeHeader(
    postView: PostView,
    instantScores: InstantScores,
    onPersonClick: (personId: PersonId) -> Unit,
    showAvatar: Boolean,
    voteDisplayMode: VoteDisplayMode,
) {
    CommentOrPostNodeHeader(
        creator = postView.creator,
        instantScores = instantScores,
        published = postView.post.published,
        updated = postView.post.updated,
        deleted = postView.post.deleted,
        onPersonClick = onPersonClick,
        isPostCreator = true,
        isCommunityBanned = postView.creator_banned_from_community,
        onClick = {},
        onLongCLick = {},
        showAvatar = showAvatar,
        voteDisplayMode = voteDisplayMode,
        isDistinguished = false,
    )
}

@Composable
fun PostTitleBlock(
    postView: PostView,
    expandedImage: Boolean,
    account: Account,
    useCustomTabs: Boolean,
    usePrivateTabs: Boolean,
    blurNSFW: BlurNSFW,
    appState: JerboaAppState,
    showIfRead: Boolean,
) {
    val imagePost = postView.post.url?.let { getPostType(it) == PostType.Image } ?: false

    if (imagePost && expandedImage) {
        PostTitleAndImageLink(
            postView = postView,
            blurNSFW = blurNSFW,
            appState = appState,
            showIfRead = showIfRead,
        )
    } else {
        PostTitleAndThumbnail(
            postView = postView,
            account = account,
            useCustomTabs = useCustomTabs,
            usePrivateTabs = usePrivateTabs,
            blurNSFW = blurNSFW,
            appState = appState,
            showIfRead = showIfRead,
        )
    }
}

@Composable
fun PostName(
    postView: PostView,
    showIfRead: Boolean,
) {
    var color =
        if (postView.post.featured_local) {
            MaterialTheme.colorScheme.primary
        } else if (postView.post.featured_community) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    if (showIfRead && postView.read) {
        color = color.muted
    }

    Text(
        text = postView.post.name,
        style = MaterialTheme.typography.headlineMedium,
        color = color,
        modifier = Modifier.testTag("jerboa:posttitle"),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostTitleAndImageLink(
    postView: PostView,
    blurNSFW: BlurNSFW,
    appState: JerboaAppState,
    showIfRead: Boolean,
) {
    // This was tested, we know it exists
    val url = postView.post.url?.toHttps()

    Column(
        modifier =
            Modifier.padding(
                vertical = MEDIUM_PADDING,
                horizontal = MEDIUM_PADDING,
            ),
    ) {
        // Title of the post
        PostName(
            postView = postView,
            showIfRead = showIfRead,
        )
    }

    url?.let { cUrl ->
        PictrsUrlImage(
            url = cUrl,
            blur = blurNSFW.needBlur(postView),
            modifier =
                Modifier
                    .combinedClickable(
                        onClick = { appState.openImageViewer(cUrl) },
                        onLongClick = { appState.showLinkPopup(cUrl) },
                    ),
        )
    }
}

@Composable
fun PostTitleAndThumbnail(
    postView: PostView,
    account: Account,
    useCustomTabs: Boolean,
    usePrivateTabs: Boolean,
    blurNSFW: BlurNSFW,
    appState: JerboaAppState,
    showIfRead: Boolean,
) {
    Column(
        modifier = Modifier.padding(horizontal = MEDIUM_PADDING),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
        ) {
            // Title of the post
            Column(
                verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
                modifier = Modifier.weight(1f),
            ) {
                PostName(postView = postView, showIfRead = showIfRead)
                postView.post.url?.also { postUrl ->
                    if (!isSameInstance(postUrl, account.instance)) {
                        val hostName = hostName(postUrl)
                        hostName?.also {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.onBackground.muted,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            ThumbnailTile(
                postView = postView,
                useCustomTabs = useCustomTabs,
                usePrivateTabs = usePrivateTabs,
                blurNSFW = blurNSFW,
                appState = appState,
            )
        }
    }
}

@Composable
fun PostBody(
    postView: PostView,
    fullBody: Boolean,
    viewSource: Boolean,
    expandedImage: Boolean,
    account: Account,
    useCustomTabs: Boolean,
    usePrivateTabs: Boolean,
    blurNSFW: BlurNSFW,
    showPostLinkPreview: Boolean,
    appState: JerboaAppState,
    clickBody: () -> Unit = {},
    showIfRead: Boolean,
) {
    val post = postView.post
    Column(
        verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
    ) {
        PostTitleBlock(
            postView = postView,
            expandedImage = expandedImage,
            account = account,
            useCustomTabs = useCustomTabs,
            usePrivateTabs = usePrivateTabs,
            blurNSFW = blurNSFW,
            appState = appState,
            showIfRead = showIfRead,
        )

        // The metadata card
        if (fullBody && showPostLinkPreview) {
            MetadataCard(post = post)
        }

        // Check to make sure body isn't empty string
        val body = post.body?.trim()?.ifEmpty { null }

        // The desc
        body?.also { text ->
            Card(
                colors = CARD_COLORS,
                shape = MaterialTheme.shapes.medium,
                modifier =
                    Modifier
                        .padding(vertical = MEDIUM_PADDING, horizontal = MEDIUM_PADDING)
                        .fillMaxWidth(),
                content = {
                    if (fullBody) {
                        Column(
                            modifier =
                                Modifier
                                    .padding(MEDIUM_PADDING),
                        ) {
                            if (viewSource) {
                                SelectionContainer {
                                    Text(
                                        text = text,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                }
                            } else {
                                MyMarkdownText(
                                    markdown = text,
                                    onClick = {},
                                )
                            }
                        }
                    } else {
                        CreateMarkdownPreview(
                            markdown = text,
                            color = LocalContentColor.current,
                            onClick = clickBody,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(MEDIUM_PADDING),
                        )
                    }
                },
            )
        }
    }
}

@Preview
@Composable
fun PreviewStoryTitleAndMetadata() {
    PostBody(
        postView = samplePostView,
        fullBody = false,
        viewSource = false,
        expandedImage = false,
        account = AnonAccount,
        useCustomTabs = false,
        usePrivateTabs = false,
        blurNSFW = BlurNSFW.NSFW,
        showPostLinkPreview = true,
        appState = rememberJerboaAppState(),
        showIfRead = true,
    )
}

@Preview
@Composable
fun PreviewSourcePost() {
    PostBody(
        postView = sampleMarkdownPostView,
        fullBody = true,
        viewSource = true,
        expandedImage = false,
        account = AnonAccount,
        useCustomTabs = false,
        usePrivateTabs = false,
        blurNSFW = BlurNSFW.NSFW,
        showPostLinkPreview = true,
        appState = rememberJerboaAppState(),
        showIfRead = true,
    )
}

@Composable
fun PostFooterLine(
    postView: PostView,
    admins: List<PersonView>,
    moderators: List<PersonId>?,
    instantScores: InstantScores,
    onUpvoteClick: () -> Unit,
    onDownvoteClick: () -> Unit,
    onReplyClick: (postView: PostView) -> Unit,
    onSaveClick: (postView: PostView) -> Unit,
    onEditPostClick: (postView: PostView) -> Unit,
    onDeletePostClick: (postView: PostView) -> Unit,
    onReportClick: (postView: PostView) -> Unit,
    onRemoveClick: (postView: PostView) -> Unit,
    onBanPersonClick: (person: Person) -> Unit,
    onBanFromCommunityClick: (banData: BanFromCommunityData) -> Unit,
    onLockPostClick: (postView: PostView) -> Unit,
    onFeaturePostClick: (data: PostFeatureData) -> Unit,
    onViewVotesClick: (PostId) -> Unit,
    onCommunityClick: (community: Community) -> Unit,
    onPersonClick: (personId: PersonId) -> Unit,
    onViewSourceClick: () -> Unit,
    modifier: Modifier = Modifier,
    showReply: Boolean = false,
    account: Account,
    enableDownVotes: Boolean,
    viewSource: Boolean,
    postActionBarMode: PostActionBarMode,
    fromPostActivity: Boolean,
    scope: CoroutineScope,
) {
    val ctx = LocalContext.current
    var showMoreOptions by remember { mutableStateOf(false) }

    if (showMoreOptions) {
        val fallbackModerators = remember(moderators) {
            moderators ?: simulateModerators(
                ctx = ctx,
                account = account,
                forCommunity = postView.community.id,
            )
        }

        val amMod = remember(moderators) {
            amMod(
                moderators = fallbackModerators,
                myId = account.id,
            )
        }

        val canMod = remember(admins, moderators) {
            canMod(
                creatorId = postView.creator.id,
                admins = admins,
                moderators = fallbackModerators,
                myId = account.id,
            )
        }

        PostOptionsDropdown(
            postView = postView,
            onDismissRequest = { showMoreOptions = false },
            onCommunityClick = onCommunityClick,
            onPersonClick = onPersonClick,
            onEditPostClick = onEditPostClick,
            onDeletePostClick = onDeletePostClick,
            onReportClick = onReportClick,
            onRemoveClick = onRemoveClick,
            onBanPersonClick = onBanPersonClick,
            onBanFromCommunityClick = onBanFromCommunityClick,
            onLockPostClick = onLockPostClick,
            onFeaturePostClick = onFeaturePostClick,
            onViewVotesClick = onViewVotesClick,
            onViewSourceClick = onViewSourceClick,
            isCreator = account.id == postView.creator.id,
            canMod = canMod,
            amAdmin = account.isAdmin,
            amMod = amMod,
            viewSource = viewSource,
            showViewSource = fromPostActivity,
            scope = scope,
        )
    }

    val horizontalArrangement =
        when (postActionBarMode) {
            PostActionBarMode.Long -> Arrangement.spacedBy(XXL_PADDING)
            PostActionBarMode.LeftHandShort -> Arrangement.spacedBy(LARGE_PADDING)
            PostActionBarMode.RightHandShort -> Arrangement.spacedBy(LARGE_PADDING)
        }

    Row(
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = SMALL_PADDING),
    ) {
        // Right handside shows the comments on the left side
        if (postActionBarMode == PostActionBarMode.RightHandShort) {
            CommentNewCountRework(
                comments = postView.counts.comments,
                unreadCount = postView.unread_comments,
                account = account,
                modifier = Modifier.weight(1F, true),
            )
        }

        VoteGeneric(
            myVote = instantScores.myVote,
            type = VoteType.Upvote,
            onVoteClick = onUpvoteClick,
            account = account,
        )
        if (enableDownVotes) {
            VoteGeneric(
                myVote = instantScores.myVote,
                type = VoteType.Downvote,
                onVoteClick = onDownvoteClick,
                account = account,
            )
        }

        if (postActionBarMode == PostActionBarMode.Long) {
            CommentNewCountRework(
                comments = postView.counts.comments,
                unreadCount = postView.unread_comments,
                account = account,
                modifier = Modifier.weight(1F, true),
            )
        }

        if (showReply) {
            ActionBarButton(
                icon = Icons.AutoMirrored.Outlined.Comment,
                contentDescription = stringResource(R.string.postListing_reply),
                onClick = { onReplyClick(postView) },
                account = account,
            )
        }
        ActionBarButton(
            icon =
                if (postView.saved) {
                    Icons.Filled.Bookmark
                } else {
                    Icons.Outlined.BookmarkBorder
                },
            contentDescription =
                if (postView.saved) {
                    stringResource(R.string.removeBookmark)
                } else {
                    stringResource(R.string.addBookmark)
                },
            onClick = { onSaveClick(postView) },
            contentColor =
                if (postView.saved) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground.muted
                },
            account = account,
        )
        ActionBarButton(
            icon = Icons.Outlined.MoreVert,
            contentDescription = stringResource(R.string.moreOptions),
            account = account,
            onClick = { showMoreOptions = !showMoreOptions },
            requiresAccount = false,
            modifier = if (postActionBarMode == PostActionBarMode.LeftHandShort) {
                Modifier.weight(
                    1F,
                    true,
                )
            } else {
                Modifier
            },
        )

        if (postActionBarMode == PostActionBarMode.LeftHandShort) {
            CommentNewCountRework(
                comments = postView.counts.comments,
                unreadCount = postView.unread_comments,
                account = account,
            )
        }
    }
}

@Composable
fun CommentNewCountRework(
    comments: Long,
    unreadCount: Long,
    account: Account,
    modifier: Modifier = Modifier,
) {
    val unread =
        if (unreadCount == 0L || comments == unreadCount) {
            null
        } else {
            (if (unreadCount > 0) "+" else "") + siFormat(unreadCount)
        }

    ActionBarButtonAndBadge(
        icon = Icons.Outlined.ChatBubbleOutline,
        iconBadgeCount = unread,
        contentDescription = null,
        text = siFormat(comments),
        noClick = true,
        account = account,
        onClick = {},
        modifier = modifier,
    )
}

@Composable
fun CommentNewCount(
    comments: Long,
    unreadCount: Long,
    style: TextStyle = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
    spacing: Dp = 0.dp,
) {
    val unread =
        if (unreadCount == 0L || comments == unreadCount) {
            null
        } else {
            unreadCount
        }
    if (unread != null) {
        Spacer(Modifier.padding(horizontal = spacing))

        Text(
            text = stringResource(R.string.post_listing_new, unread),
            style = style,
            color = MaterialTheme.colorScheme.onSurface.muted,
        )
    }
}

@Preview
@Composable
fun CommentCountPreview() {
    CommentNewCountRework(42, 0, account = AnonAccount)
}

@Preview
@Composable
fun PostFooterLinePreview() {
    val postView = samplePostView
    val instantScores =
        InstantScores(
            myVote = postView.my_vote,
            score = postView.counts.score,
            upvotes = postView.counts.upvotes,
            downvotes = postView.counts.downvotes,
        )
    PostFooterLine(
        postView = postView,
        admins = emptyList(),
        moderators = emptyList(),
        instantScores = instantScores,
        onUpvoteClick = {},
        onDownvoteClick = {},
        onReplyClick = {},
        onSaveClick = {},
        onEditPostClick = {},
        onDeletePostClick = {},
        onReportClick = {},
        onRemoveClick = {},
        onBanPersonClick = {},
        onBanFromCommunityClick = {},
        onLockPostClick = {},
        onFeaturePostClick = {},
        onViewVotesClick = {},
        onCommunityClick = {},
        onPersonClick = {},
        onViewSourceClick = {},
        account = AnonAccount,
        enableDownVotes = true,
        viewSource = false,
        postActionBarMode = PostActionBarMode.Long,
        fromPostActivity = true,
        scope = rememberCoroutineScope(),
    )
}

@Preview
@Composable
fun PreviewPostListingCard() {
    PostListing(
        postView = samplePostView,
        admins = emptyList(),
        moderators = emptyList(),
        useCustomTabs = false,
        usePrivateTabs = false,
        onUpvoteClick = {},
        onDownvoteClick = {},
        onReplyClick = {},
        onPostClick = {},
        onSaveClick = {},
        onCommunityClick = {},
        onEditPostClick = {},
        onDeletePostClick = {},
        onReportClick = {},
        onRemoveClick = {},
        onBanPersonClick = {},
        onBanFromCommunityClick = {},
        onLockPostClick = {},
        onFeaturePostClick = {},
        onViewVotesClick = {},
        onPersonClick = {},
        fullBody = false,
        account = AnonAccount,
        postViewMode = PostViewMode.Card,
        showVotingArrowsInListView = true,
        enableDownVotes = true,
        showAvatar = true,
        blurNSFW = BlurNSFW.NSFW,
        appState = rememberJerboaAppState(),
        showPostLinkPreview = true,
        showIfRead = true,
        voteDisplayMode = VoteDisplayMode.Full,
        postActionBarMode = PostActionBarMode.Long,
        swipeToActionPreset = SwipeToActionPreset.TwoSides,
    )
}

@Preview
@Composable
fun PreviewLinkPostListing() {
    PostListing(
        postView = sampleLinkPostView,
        admins = emptyList(),
        moderators = emptyList(),
        useCustomTabs = false,
        usePrivateTabs = false,
        onUpvoteClick = {},
        onDownvoteClick = {},
        onReplyClick = {},
        onPostClick = {},
        onSaveClick = {},
        onCommunityClick = {},
        onEditPostClick = {},
        onDeletePostClick = {},
        onReportClick = {},
        onRemoveClick = {},
        onBanPersonClick = {},
        onBanFromCommunityClick = {},
        onLockPostClick = {},
        onFeaturePostClick = {},
        onViewVotesClick = {},
        onPersonClick = {},
        fullBody = false,
        account = AnonAccount,
        postViewMode = PostViewMode.Card,
        showVotingArrowsInListView = true,
        enableDownVotes = true,
        showAvatar = true,
        blurNSFW = BlurNSFW.NSFW,
        appState = rememberJerboaAppState(),
        showPostLinkPreview = true,
        showIfRead = true,
        voteDisplayMode = VoteDisplayMode.Full,
        postActionBarMode = PostActionBarMode.Long,
        swipeToActionPreset = SwipeToActionPreset.TwoSides,
    )
}

@Preview
@Composable
fun PreviewImagePostListingCard() {
    PostListing(
        postView = sampleImagePostView,
        admins = emptyList(),
        moderators = emptyList(),
        useCustomTabs = false,
        usePrivateTabs = false,
        onUpvoteClick = {},
        onDownvoteClick = {},
        onReplyClick = {},
        onPostClick = {},
        onSaveClick = {},
        onCommunityClick = {},
        onEditPostClick = {},
        onDeletePostClick = {},
        onReportClick = {},
        onRemoveClick = {},
        onBanPersonClick = {},
        onBanFromCommunityClick = {},
        onLockPostClick = {},
        onFeaturePostClick = {},
        onViewVotesClick = {},
        onPersonClick = {},
        fullBody = false,
        account = AnonAccount,
        postViewMode = PostViewMode.Card,
        showVotingArrowsInListView = true,
        enableDownVotes = true,
        showAvatar = true,
        blurNSFW = BlurNSFW.NSFW,
        appState = rememberJerboaAppState(),
        showPostLinkPreview = true,
        showIfRead = true,
        voteDisplayMode = VoteDisplayMode.Full,
        postActionBarMode = PostActionBarMode.Long,
        swipeToActionPreset = SwipeToActionPreset.TwoSides,
    )
}

@Preview
@Composable
fun PreviewImagePostListingSmallCard() {
    PostListing(
        postView = sampleImagePostView,
        admins = emptyList(),
        moderators = emptyList(),
        useCustomTabs = false,
        usePrivateTabs = false,
        onUpvoteClick = {},
        onDownvoteClick = {},
        onReplyClick = {},
        onPostClick = {},
        onSaveClick = {},
        onCommunityClick = {},
        onEditPostClick = {},
        onDeletePostClick = {},
        onReportClick = {},
        onRemoveClick = {},
        onBanPersonClick = {},
        onBanFromCommunityClick = {},
        onLockPostClick = {},
        onFeaturePostClick = {},
        onViewVotesClick = {},
        onPersonClick = {},
        fullBody = false,
        account = AnonAccount,
        postViewMode = PostViewMode.SmallCard,
        showVotingArrowsInListView = true,
        enableDownVotes = true,
        showAvatar = true,
        blurNSFW = BlurNSFW.NSFW,
        appState = rememberJerboaAppState(),
        showPostLinkPreview = true,
        showIfRead = true,
        voteDisplayMode = VoteDisplayMode.Full,
        postActionBarMode = PostActionBarMode.Long,
        swipeToActionPreset = SwipeToActionPreset.TwoSides,
    )
}

@Preview
@Composable
fun PreviewLinkNoThumbnailPostListing() {
    PostListing(
        postView = sampleLinkNoThumbnailPostView,
        admins = emptyList(),
        moderators = emptyList(),
        useCustomTabs = false,
        usePrivateTabs = false,
        onUpvoteClick = {},
        onDownvoteClick = {},
        onReplyClick = {},
        onPostClick = {},
        onSaveClick = {},
        onCommunityClick = {},
        onEditPostClick = {},
        onDeletePostClick = {},
        onReportClick = {},
        onRemoveClick = {},
        onBanPersonClick = {},
        onBanFromCommunityClick = {},
        onLockPostClick = {},
        onFeaturePostClick = {},
        onViewVotesClick = {},
        onPersonClick = {},
        fullBody = false,
        account = AnonAccount,
        postViewMode = PostViewMode.Card,
        showVotingArrowsInListView = true,
        enableDownVotes = true,
        showAvatar = true,
        blurNSFW = BlurNSFW.NSFW,
        appState = rememberJerboaAppState(),
        showPostLinkPreview = true,
        showIfRead = true,
        voteDisplayMode = VoteDisplayMode.Full,
        postActionBarMode = PostActionBarMode.Long,
        swipeToActionPreset = SwipeToActionPreset.TwoSides,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListing(
    postView: PostView,
    admins: List<PersonView>,
    moderators: List<PersonId>?,
    useCustomTabs: Boolean,
    usePrivateTabs: Boolean,
    onUpvoteClick: (postView: PostView) -> Unit,
    onDownvoteClick: (postView: PostView) -> Unit,
    onReplyClick: (postView: PostView) -> Unit = {},
    onPostClick: (postView: PostView) -> Unit,
    onSaveClick: (postView: PostView) -> Unit,
    onCommunityClick: (community: Community) -> Unit,
    onEditPostClick: (postView: PostView) -> Unit,
    onDeletePostClick: (postView: PostView) -> Unit,
    onReportClick: (postView: PostView) -> Unit,
    onRemoveClick: (postView: PostView) -> Unit,
    onBanPersonClick: (person: Person) -> Unit,
    onBanFromCommunityClick: (banData: BanFromCommunityData) -> Unit,
    onLockPostClick: (postView: PostView) -> Unit,
    onFeaturePostClick: (data: PostFeatureData) -> Unit,
    onPersonClick: (personId: PersonId) -> Unit,
    onViewVotesClick: (PostId) -> Unit,
    showReply: Boolean = false,
    showCommunityName: Boolean = true,
    fullBody: Boolean,
    account: Account,
    postViewMode: PostViewMode,
    showVotingArrowsInListView: Boolean,
    enableDownVotes: Boolean,
    showAvatar: Boolean,
    blurNSFW: BlurNSFW,
    appState: JerboaAppState,
    showPostLinkPreview: Boolean,
    showIfRead: Boolean,
    voteDisplayMode: VoteDisplayMode,
    postActionBarMode: PostActionBarMode,
    swipeToActionPreset: SwipeToActionPreset,
) {
    val ctx = LocalContext.current
    // This stores vote data
    var instantScores by
        remember {
            mutableStateOf(
                InstantScores(
                    myVote = postView.my_vote,
                    score = postView.counts.score,
                    upvotes = postView.counts.upvotes,
                    downvotes = postView.counts.downvotes,
                ),
            )
        }

    var viewSource by remember { mutableStateOf(false) }

    val swipeAction: (action: SwipeToActionType) -> Unit = remember(postView) {
        {
            if (account.isReadyAndIfNotShowSimplifiedInfoToast(ctx)) {
                when (it) {
                    SwipeToActionType.Upvote -> {
                        instantScores =
                            instantScores.update(VoteType.Upvote)
                        onUpvoteClick(postView)
                    }

                    SwipeToActionType.Downvote -> {
                        instantScores =
                            instantScores.update(VoteType.Downvote)
                        onDownvoteClick(postView)
                    }

                    SwipeToActionType.Reply -> {
                        onReplyClick(postView)
                    }

                    SwipeToActionType.Save -> {
                        onSaveClick(postView)
                    }
                }
            }
        }
    }

    val swipeState = rememberSwipeActionState(
        swipeToActionPreset = swipeToActionPreset,
        enableDownVotes = enableDownVotes,
        onAction = swipeAction,
        rememberKey = postView,
    )

    val swipeableContent: @Composable RowScope.() -> Unit = {
        Row {
            when (postViewMode) {
                PostViewMode.Card ->
                    PostListingCard(
                        postView = postView,
                        admins = admins,
                        moderators = moderators,
                        instantScores = instantScores,
                        onUpvoteClick = {
                            instantScores = instantScores.update(VoteType.Upvote)
                            onUpvoteClick(postView)
                        },
                        onDownvoteClick = {
                            instantScores = instantScores.update(VoteType.Downvote)
                            onDownvoteClick(postView)
                        },
                        onReplyClick = onReplyClick,
                        onPostClick = onPostClick,
                        onSaveClick = onSaveClick,
                        onCommunityClick = onCommunityClick,
                        onEditPostClick = onEditPostClick,
                        onDeletePostClick = onDeletePostClick,
                        onReportClick = onReportClick,
                        onRemoveClick = onRemoveClick,
                        onBanPersonClick = onBanPersonClick,
                        onBanFromCommunityClick = onBanFromCommunityClick,
                        onLockPostClick = onLockPostClick,
                        onFeaturePostClick = onFeaturePostClick,
                        onViewVotesClick = onViewVotesClick,
                        onPersonClick = onPersonClick,
                        onViewSourceClick = {
                            viewSource = !viewSource
                        },
                        viewSource = viewSource,
                        showReply = showReply,
                        showCommunityName = showCommunityName,
                        fullBody = fullBody,
                        account = account,
                        expandedImage = true,
                        enableDownVotes = enableDownVotes,
                        showAvatar = showAvatar,
                        useCustomTabs = useCustomTabs,
                        usePrivateTabs = usePrivateTabs,
                        blurNSFW = blurNSFW,
                        showPostLinkPreview = showPostLinkPreview,
                        appState = appState,
                        showIfRead = showIfRead,
                        voteDisplayMode = voteDisplayMode,
                        postActionBarMode = postActionBarMode,
                    )

                PostViewMode.SmallCard ->
                    PostListingCard(
                        postView = postView,
                        admins = admins,
                        moderators = moderators,
                        instantScores = instantScores,
                        onUpvoteClick = {
                            instantScores = instantScores.update(VoteType.Upvote)
                            onUpvoteClick(postView)
                        },
                        onDownvoteClick = {
                            instantScores = instantScores.update(VoteType.Downvote)
                            onDownvoteClick(postView)
                        },
                        onReplyClick = onReplyClick,
                        onPostClick = onPostClick,
                        onSaveClick = onSaveClick,
                        onCommunityClick = onCommunityClick,
                        onEditPostClick = onEditPostClick,
                        onDeletePostClick = onDeletePostClick,
                        onReportClick = onReportClick,
                        onRemoveClick = onRemoveClick,
                        onBanPersonClick = onBanPersonClick,
                        onBanFromCommunityClick = onBanFromCommunityClick,
                        onLockPostClick = onLockPostClick,
                        onFeaturePostClick = onFeaturePostClick,
                        onViewVotesClick = onViewVotesClick,
                        onPersonClick = onPersonClick,
                        onViewSourceClick = {
                            viewSource = !viewSource
                        },
                        viewSource = viewSource,
                        showReply = showReply,
                        showCommunityName = showCommunityName,
                        fullBody = false,
                        account = account,
                        expandedImage = false,
                        enableDownVotes = enableDownVotes,
                        showAvatar = showAvatar,
                        useCustomTabs = useCustomTabs,
                        usePrivateTabs = usePrivateTabs,
                        blurNSFW = blurNSFW,
                        showPostLinkPreview = showPostLinkPreview,
                        appState = appState,
                        voteDisplayMode = voteDisplayMode,
                        postActionBarMode = postActionBarMode,
                    )

                PostViewMode.List ->
                    PostListingList(
                        postView = postView,
                        instantScores = instantScores,
                        onUpvoteClick = {
                            instantScores = instantScores.update(VoteType.Upvote)
                            onUpvoteClick(postView)
                        },
                        onDownvoteClick = {
                            instantScores = instantScores.update(VoteType.Downvote)
                            onDownvoteClick(postView)
                        },
                        onPostClick = onPostClick,
                        showCommunityName = showCommunityName,
                        account = account,
                        showVotingArrowsInListView = showVotingArrowsInListView,
                        useCustomTabs = useCustomTabs,
                        usePrivateTabs = usePrivateTabs,
                        blurNSFW = blurNSFW,
                        appState = appState,
                        showIfRead = showIfRead,
                        enableDownVotes = enableDownVotes,
                        voteDisplayMode = voteDisplayMode,
                    )
            }
        }
    }

    if (swipeToActionPreset != SwipeToActionPreset.Disabled) {
        SwipeToAction(
            swipeToActionPreset = swipeToActionPreset,
            enableDownVotes = enableDownVotes,
            swipeableContent = swipeableContent,
            swipeState = swipeState,
        )
    } else {
        Row {
            swipeableContent()
        }
    }
}

@Composable
fun PostVotingTile(
    instantScores: InstantScores,
    onUpvoteClick: () -> Unit,
    onDownvoteClick: () -> Unit,
    account: Account,
    enableDownVotes: Boolean,
    voteDisplayMode: VoteDisplayMode,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
        modifier =
            Modifier
                .fillMaxHeight()
                .padding(end = MEDIUM_PADDING),
    ) {
        VoteGeneric(
            myVote = instantScores.myVote,
            type = VoteType.Upvote,
            onVoteClick = onUpvoteClick,
            account = account,
        )

        val scoreOrPctStr = instantScores.scoreOrPctStr(voteDisplayMode)

        Text(
            text = scoreOrPctStr ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = scoreColor(myVote = instantScores.myVote),
            // Hide the vote number if its
            modifier = Modifier.alpha(if (scoreOrPctStr != null) 1f else 0f),
        )

        if (enableDownVotes) {
            // invisible Text below aligns width of PostVotingTiles
            Text(
                text = "00000",
                modifier = Modifier.height(0.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
            VoteGeneric(
                myVote = instantScores.myVote,
                type = VoteType.Downvote,
                onVoteClick = onDownvoteClick,
                account = account,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostListingList(
    postView: PostView,
    instantScores: InstantScores,
    onUpvoteClick: () -> Unit,
    onDownvoteClick: () -> Unit,
    onPostClick: (postView: PostView) -> Unit,
    showCommunityName: Boolean = true,
    account: Account,
    showVotingArrowsInListView: Boolean,
    useCustomTabs: Boolean,
    usePrivateTabs: Boolean,
    blurNSFW: BlurNSFW,
    appState: JerboaAppState,
    showIfRead: Boolean,
    enableDownVotes: Boolean,
    voteDisplayMode: VoteDisplayMode,
) {
    Column(
        modifier =
            Modifier
                .padding(
                    horizontal = MEDIUM_PADDING,
                    vertical = MEDIUM_PADDING,
                )
                .testTag("jerboa:post"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    SMALL_PADDING,
                ),
        ) {
            if (showVotingArrowsInListView) {
                PostVotingTile(
                    instantScores = instantScores,
                    onUpvoteClick = onUpvoteClick,
                    onDownvoteClick = onDownvoteClick,
                    account = account,
                    enableDownVotes = enableDownVotes,
                    voteDisplayMode = voteDisplayMode,
                )
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable { onPostClick(postView) },
                verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
            ) {
                PostName(postView = postView, showIfRead = showIfRead)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(SMALLER_PADDING, Alignment.Start),
                ) {
                    // You must use a center align modifier for each of these
                    val centerMod = Modifier.align(Alignment.CenterVertically)
                    if (showCommunityName) {
                        CommunityLink(
                            community = postView.community,
                            onClick = {},
                            clickable = false,
                            showDefaultIcon = false,
                            showAvatar = false,
                            blurNSFW = blurNSFW,
                            modifier = centerMod,
                        )
                        DotSpacer(modifier = centerMod)
                    }
                    PersonProfileLink(
                        person = postView.creator,
                        onClick = {},
                        clickable = false,
                        color = MaterialTheme.colorScheme.onSurface.muted,
                        showAvatar = false,
                        modifier = centerMod,
                    )
                    DotSpacer(modifier = centerMod)
                    postView.post.url?.also { postUrl ->
                        if (!isSameInstance(postUrl, account.instance)) {
                            val hostName = hostName(postUrl)
                            hostName?.also {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.onBackground.muted,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = centerMod,
                                )
                                DotSpacer(modifier = centerMod)
                            }
                        }
                    }
                    TimeAgo(
                        published = postView.post.published,
                        updated = postView.post.updated,
                        modifier = centerMod,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SMALLER_PADDING),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!showVotingArrowsInListView) {
                        Text(
                            text = instantScores.score.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scoreColor(myVote = instantScores.myVote),
                        )
                        DotSpacer()
                    }
                    Text(
                        text =
                            stringResource(
                                R.string.post_listing_comments_count,
                                postView.counts.comments,
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.muted,
                    )
                    CommentNewCount(
                        comments = postView.counts.comments,
                        unreadCount = postView.unread_comments,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    NsfwBadge(nsfwCheck(postView))
                }
            }
            ThumbnailTile(
                postView = postView,
                useCustomTabs = useCustomTabs,
                usePrivateTabs = usePrivateTabs,
                blurNSFW = blurNSFW,
                appState = appState,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThumbnailTile(
    postView: PostView,
    useCustomTabs: Boolean,
    usePrivateTabs: Boolean,
    blurNSFW: BlurNSFW,
    appState: JerboaAppState,
) {
    postView.post.url?.also { url ->
        val postType = getPostType(url)

        val postLinkPicMod =
            Modifier
                .size(POST_LINK_PIC_SIZE)
                .combinedClickable(
                    onClick = {
                        if (postType != PostType.Link) {
                            appState.openImageViewer(url)
                        } else {
                            appState.openLink(
                                url,
                                useCustomTabs,
                                usePrivateTabs,
                            )
                        }
                    },
                    onLongClick = {
                        appState.showLinkPopup(url)
                    },
                )

        Box {
            postView.post.thumbnail_url?.also { thumbnail ->
                PictrsThumbnailImage(
                    thumbnail = thumbnail,
                    blur = blurNSFW.needBlur(postView),
                    roundBottomEndCorner = postType != PostType.Link,
                    modifier = postLinkPicMod,
                )
            } ?: run {
                Card(
                    colors = CARD_COLORS,
                    modifier = postLinkPicMod,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = null,
                            modifier = Modifier.size(LINK_ICON_SIZE),
                        )
                    }
                }
            }

            // Display a caret in the bottom right corner to denote this as an image
            if (postType != PostType.Link) {
                Icon(
                    painter = painterResource(id = R.drawable.triangle),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(THUMBNAIL_CARET_SIZE)
                            .align(Alignment.BottomEnd),
                    tint =
                        when (postType) {
                            PostType.Video -> MaterialTheme.jerboaColorScheme.videoHighlight
                            else -> MaterialTheme.jerboaColorScheme.imageHighlight
                        },
                )
            }
        }
    }
}

@Preview
@Composable
fun PostListingListPreview() {
    val postView = samplePostView
    val instantScores =
        InstantScores(
            myVote = postView.my_vote,
            score = postView.counts.score,
            upvotes = postView.counts.upvotes,
            downvotes = postView.counts.downvotes,
        )
    PostListingList(
        postView = postView,
        instantScores = instantScores,
        onUpvoteClick = {},
        onDownvoteClick = {},
        onPostClick = {},
        account = AnonAccount,
        showVotingArrowsInListView = true,
        useCustomTabs = false,
        usePrivateTabs = false,
        blurNSFW = BlurNSFW.NSFW,
        appState = rememberJerboaAppState(),
        showIfRead = true,
        enableDownVotes = false,
        voteDisplayMode = VoteDisplayMode.Full,
    )
}

@Preview
@Composable
fun PostListingListWithThumbPreview() {
    val postView = sampleImagePostView
    val instantScores =
        InstantScores(
            myVote = postView.my_vote,
            score = postView.counts.score,
            upvotes = postView.counts.upvotes,
            downvotes = postView.counts.downvotes,
        )
    PostListingList(
        postView = postView,
        instantScores = instantScores,
        onUpvoteClick = {},
        onDownvoteClick = {},
        onPostClick = {},
        account = AnonAccount,
        showVotingArrowsInListView = true,
        useCustomTabs = false,
        usePrivateTabs = false,
        blurNSFW = BlurNSFW.NSFW,
        appState = rememberJerboaAppState(),
        showIfRead = true,
        enableDownVotes = false,
        voteDisplayMode = VoteDisplayMode.Full,
    )
}

@Composable
fun PostListingCard(
    postView: PostView,
    admins: List<PersonView>,
    moderators: List<PersonId>?,
    instantScores: InstantScores,
    onUpvoteClick: () -> Unit,
    onDownvoteClick: () -> Unit,
    onReplyClick: (postView: PostView) -> Unit = {},
    onPostClick: (postView: PostView) -> Unit,
    onSaveClick: (postView: PostView) -> Unit,
    onCommunityClick: (community: Community) -> Unit,
    onEditPostClick: (postView: PostView) -> Unit,
    onDeletePostClick: (postView: PostView) -> Unit,
    onReportClick: (postView: PostView) -> Unit,
    onRemoveClick: (postView: PostView) -> Unit,
    onBanPersonClick: (person: Person) -> Unit,
    onBanFromCommunityClick: (banData: BanFromCommunityData) -> Unit,
    onLockPostClick: (postView: PostView) -> Unit,
    onFeaturePostClick: (data: PostFeatureData) -> Unit,
    onPersonClick: (personId: PersonId) -> Unit,
    onViewVotesClick: (PostId) -> Unit,
    onViewSourceClick: () -> Unit,
    viewSource: Boolean,
    showReply: Boolean = false,
    showCommunityName: Boolean = true,
    fullBody: Boolean,
    account: Account,
    expandedImage: Boolean,
    enableDownVotes: Boolean,
    showAvatar: Boolean,
    useCustomTabs: Boolean,
    usePrivateTabs: Boolean,
    blurNSFW: BlurNSFW,
    showPostLinkPreview: Boolean,
    appState: JerboaAppState,
    showIfRead: Boolean = false,
    voteDisplayMode: VoteDisplayMode,
    postActionBarMode: PostActionBarMode,
) {
    Column(
        modifier =
            Modifier
                .padding(vertical = MEDIUM_PADDING)
                .clickable { onPostClick(postView) }
                .testTag("jerboa:post"),
        // see https://stackoverflow.com/questions/77010371/prevent-popup-from-adding-padding-in-a-column-with-arrangement-spacedbylarge-p
        // verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
    ) {
        // Header
        PostHeaderLine(
            postView = postView,
            instantScores = instantScores,
            onCommunityClick = onCommunityClick,
            onPersonClick = onPersonClick,
            showCommunityName = showCommunityName,
            modifier = Modifier.padding(horizontal = MEDIUM_PADDING),
            showAvatar = showAvatar,
            blurNSFW = blurNSFW,
            voteDisplayMode = voteDisplayMode,
            fullBody = fullBody,
        )

        Spacer(modifier = Modifier.padding(vertical = MEDIUM_PADDING))

        //  Title + metadata
        PostBody(
            postView = postView,
            fullBody = fullBody,
            viewSource = viewSource,
            expandedImage = expandedImage,
            account = account,
            useCustomTabs = useCustomTabs,
            usePrivateTabs = usePrivateTabs,
            blurNSFW = blurNSFW,
            showPostLinkPreview = showPostLinkPreview,
            appState = appState,
            clickBody = { onPostClick(postView) },
            showIfRead = showIfRead,
        )

        Spacer(modifier = Modifier.padding(vertical = MEDIUM_PADDING))

        // Footer bar
        PostFooterLine(
            postView = postView,
            admins = admins,
            moderators = moderators,
            instantScores = instantScores,
            onUpvoteClick = onUpvoteClick,
            onDownvoteClick = onDownvoteClick,
            onReplyClick = onReplyClick,
            onSaveClick = onSaveClick,
            onEditPostClick = onEditPostClick,
            onDeletePostClick = onDeletePostClick,
            onReportClick = onReportClick,
            onRemoveClick = onRemoveClick,
            onBanPersonClick = onBanPersonClick,
            onBanFromCommunityClick = onBanFromCommunityClick,
            onLockPostClick = onLockPostClick,
            onFeaturePostClick = onFeaturePostClick,
            onViewVotesClick = onViewVotesClick,
            onCommunityClick = onCommunityClick,
            onPersonClick = onPersonClick,
            onViewSourceClick = onViewSourceClick,
            modifier = Modifier.padding(horizontal = MEDIUM_PADDING),
            showReply = showReply,
            account = account,
            enableDownVotes = enableDownVotes,
            viewSource = viewSource,
            postActionBarMode = postActionBarMode,
            fromPostActivity = fullBody,
            scope = appState.coroutineScope,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PostListingHeaderPreview() {
    SimpleTopAppBar("Post", onClickBack = {})
}

@Composable
fun MetadataCard(post: Post) {
    val embedTitle = post.embed_title
    if (embedTitle != null) {
        OutlinedCard(
            shape = MaterialTheme.shapes.medium,
            modifier =
                Modifier
                    .padding(vertical = MEDIUM_PADDING, horizontal = MEDIUM_PADDING)
                    .fillMaxWidth(),
            content = {
                Column(
                    modifier = Modifier.padding(MEDIUM_PADDING),
                ) {
                    if (post.name != embedTitle) {
                        Text(
                            text = embedTitle,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (post.embed_description != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = LARGE_PADDING))
                        }
                    }
                    post.embed_description?.let {
                        // This is actually html, but markdown can render it
                        MyMarkdownText(
                            markdown = it,
                            onClick = {},
                        )
                    }
                }
            },
        )
    }
}
