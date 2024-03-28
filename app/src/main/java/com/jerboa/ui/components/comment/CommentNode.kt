package com.jerboa.ui.components.comment

import android.view.View
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jerboa.Border
import com.jerboa.CommentNode
import com.jerboa.MissingCommentNode
import com.jerboa.R
import com.jerboa.border
import com.jerboa.buildCommentsTree
import com.jerboa.calculateCommentOffset
import com.jerboa.datatypes.BanFromCommunityData
import com.jerboa.datatypes.VoteDisplayMode
import com.jerboa.datatypes.getContent
import com.jerboa.datatypes.sampleCommentView
import com.jerboa.datatypes.sampleCommunity
import com.jerboa.datatypes.samplePost
import com.jerboa.datatypes.sampleReplyCommentView
import com.jerboa.datatypes.sampleSecondReplyCommentView
import com.jerboa.db.entity.Account
import com.jerboa.db.entity.AnonAccount
import com.jerboa.feat.BlurNSFW
import com.jerboa.feat.InstantScores
import com.jerboa.feat.SwipeToActionPreset
import com.jerboa.feat.SwipeToActionType
import com.jerboa.feat.VoteType
import com.jerboa.feat.amMod
import com.jerboa.feat.canMod
import com.jerboa.feat.isReadyAndIfNotShowSimplifiedInfoToast
import com.jerboa.isPostCreator
import com.jerboa.ui.components.common.ActionBarButton
import com.jerboa.ui.components.common.CommentOrPostNodeHeader
import com.jerboa.ui.components.common.MarkdownHelper
import com.jerboa.ui.components.common.MyMarkdownText
import com.jerboa.ui.components.common.SwipeToAction
import com.jerboa.ui.components.common.VoteGeneric
import com.jerboa.ui.components.common.rememberSwipeActionState
import com.jerboa.ui.components.community.CommunityLink
import com.jerboa.ui.theme.BORDER_WIDTH
import com.jerboa.ui.theme.LARGE_PADDING
import com.jerboa.ui.theme.MEDIUM_PADDING
import com.jerboa.ui.theme.SMALL_PADDING
import com.jerboa.ui.theme.XXL_PADDING
import com.jerboa.ui.theme.colorList
import com.jerboa.ui.theme.muted
import it.vercruysse.lemmyapi.v0x19.datatypes.Comment
import it.vercruysse.lemmyapi.v0x19.datatypes.CommentId
import it.vercruysse.lemmyapi.v0x19.datatypes.CommentView
import it.vercruysse.lemmyapi.v0x19.datatypes.Community
import it.vercruysse.lemmyapi.v0x19.datatypes.Person
import it.vercruysse.lemmyapi.v0x19.datatypes.PersonId
import it.vercruysse.lemmyapi.v0x19.datatypes.PersonView
import it.vercruysse.lemmyapi.v0x19.datatypes.Post
import it.vercruysse.lemmyapi.v0x19.datatypes.PostId

@Composable
fun CommentNodeHeader(
    commentView: CommentView,
    onPersonClick: (personId: PersonId) -> Unit,
    instantScores: InstantScores,
    collapsedCommentsCount: Long,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    showAvatar: Boolean,
    voteDisplayMode: VoteDisplayMode,
) {
    CommentOrPostNodeHeader(
        creator = commentView.creator,
        instantScores = instantScores,
        voteDisplayMode = voteDisplayMode,
        published = commentView.comment.published,
        updated = commentView.comment.updated,
        deleted = commentView.comment.deleted,
        onPersonClick = onPersonClick,
        isPostCreator = isPostCreator(commentView),
        isCommunityBanned = commentView.creator_banned_from_community,
        collapsedCommentsCount = collapsedCommentsCount,
        isExpanded = isExpanded,
        onClick = onClick,
        onLongCLick = onLongClick,
        showAvatar = showAvatar,
        isDistinguished = commentView.comment.distinguished,
    )
}

@Preview
@Composable
fun CommentNodeHeaderPreview() {
    CommentNodeHeader(
        commentView = sampleCommentView,
        instantScores = InstantScores(
            score = 23,
            upvotes = 21,
            downvotes = 2,
            myVote = 26,
        ),
        voteDisplayMode = VoteDisplayMode.Full,
        onPersonClick = {},
        onClick = {},
        onLongClick = {},
        collapsedCommentsCount = 5,
        isExpanded = false,
        showAvatar = true,
    )
}

@Composable
fun CommentBody(
    comment: Comment,
    viewSource: Boolean,
    onClick: () -> Unit,
    onLongClick: ((View) -> Boolean),
) {
    if (viewSource) {
        SelectionContainer {
            Text(
                text = comment.getContent(),
                fontFamily = FontFamily.Monospace,
            )
        }
    } else {
        MyMarkdownText(
            markdown = comment.getContent(),
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, MEDIUM_PADDING),
        )
    }
}

@Preview
@Composable
fun CommentBodyPreview() {
    CommentBody(
        comment = sampleCommentView.comment,
        viewSource = false,
        onClick = {},
        onLongClick = { true },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
fun LazyListScope.commentNodeItem(
    node: CommentNode,
    admins: List<PersonView>,
    moderators: List<PersonId>?,
    increaseLazyListIndexTracker: () -> Unit,
    addToParentIndexes: () -> Unit,
    isFlat: Boolean,
    isExpanded: (commentId: CommentId) -> Boolean,
    toggleExpanded: (commentId: CommentId) -> Unit,
    toggleActionBar: (commentId: CommentId) -> Unit,
    onUpvoteClick: (commentView: CommentView) -> Unit,
    onDownvoteClick: (commentView: CommentView) -> Unit,
    onReplyClick: (commentView: CommentView) -> Unit,
    onSaveClick: (commentView: CommentView) -> Unit,
    onMarkAsReadClick: (commentView: CommentView) -> Unit,
    onCommentClick: (commentView: CommentView) -> Unit,
    onEditCommentClick: (commentView: CommentView) -> Unit,
    onDeleteCommentClick: (commentView: CommentView) -> Unit,
    onPersonClick: (personId: PersonId) -> Unit,
    onViewVotesClick: (CommentId) -> Unit,
    onHeaderClick: (commentView: CommentView) -> Unit,
    onHeaderLongClick: (commentView: CommentView) -> Unit,
    onCommunityClick: (community: Community) -> Unit,
    onPostClick: (postId: PostId) -> Unit,
    onReportClick: (commentView: CommentView) -> Unit,
    onRemoveClick: (commentView: CommentView) -> Unit,
    onDistinguishClick: (commentView: CommentView) -> Unit,
    onBanPersonClick: (person: Person) -> Unit,
    onBanFromCommunityClick: (banData: BanFromCommunityData) -> Unit,
    onCommentLinkClick: (commentView: CommentView) -> Unit,
    onBlockCreatorClick: (creator: Person) -> Unit,
    onFetchChildrenClick: (commentView: CommentView) -> Unit,
    showCollapsedCommentContent: Boolean,
    showPostAndCommunityContext: Boolean = false,
    account: Account,
    isCollapsedByParent: Boolean,
    showActionBar: (commentId: CommentId) -> Boolean,
    enableDownVotes: Boolean,
    showAvatar: Boolean,
    blurNSFW: BlurNSFW,
    voteDisplayMode: VoteDisplayMode,
    swipeToActionPreset: SwipeToActionPreset,
) {
    val commentView = node.commentView
    val commentId = commentView.comment.id

    val offset = calculateCommentOffset(node.depth) // The ones with a border on
    val offset2 =
        if (node.depth == 0) {
            MEDIUM_PADDING
        } else {
            XXL_PADDING
        }

    if (node.depth == 0) {
        addToParentIndexes()
    }

    val showMoreChildren =
        isExpanded(commentId) && node.children.isEmpty() &&
            commentView.counts.child_count > 0 && !isFlat

    increaseLazyListIndexTracker()
    // TODO Needs a contentType
    // possibly "contentNodeItemL${node.depth}"
    item(key = commentId) {
        var viewSource by remember { mutableStateOf(false) }

        val backgroundColor = MaterialTheme.colorScheme.background
        val borderColor = calculateBorderColor(backgroundColor, node.depth)
        val border = Border(BORDER_WIDTH, borderColor)

        val ctx = LocalContext.current

        var instantScores by
            remember {
                mutableStateOf(
                    InstantScores(
                        myVote = commentView.my_vote,
                        score = commentView.counts.score,
                        upvotes = commentView.counts.upvotes,
                        downvotes = commentView.counts.downvotes,
                    ),
                )
            }

        val swipeState = rememberSwipeActionState(
            swipeToActionPreset = swipeToActionPreset,
            enableDownVotes = enableDownVotes,
            rememberKey = commentView,
        ) {
            if (account.isReadyAndIfNotShowSimplifiedInfoToast(ctx)) {
                when (it) {
                    SwipeToActionType.Upvote -> {
                        instantScores =
                            instantScores.update(VoteType.Upvote)
                        onUpvoteClick(commentView)
                    }
                    SwipeToActionType.Downvote -> {
                        instantScores =
                            instantScores.update(VoteType.Downvote)
                        onDownvoteClick(commentView)
                    }
                    SwipeToActionType.Reply -> {
                        onReplyClick(commentView)
                    }
                    SwipeToActionType.Save -> {
                        onSaveClick(commentView)
                    }
                }
            }
        }
        val swipeableContent: @Composable RowScope.() -> Unit = {
            AnimatedVisibility(
                visible = !isCollapsedByParent,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(
                                start = offset,
                            )
                            .border(start = border)
                            .padding(bottom = MEDIUM_PADDING),
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                start = offset2,
                                end = MEDIUM_PADDING,
                            ),
                    ) {
                        if (showPostAndCommunityContext) {
                            PostAndCommunityContextHeader(
                                post = commentView.post,
                                community = commentView.community,
                                onCommunityClick = onCommunityClick,
                                onPostClick = onPostClick,
                                blurNSFW = blurNSFW,
                                showAvatar = showAvatar,
                            )
                        }
                        CommentNodeHeader(
                            commentView = commentView,
                            onPersonClick = onPersonClick,
                            instantScores = instantScores,
                            voteDisplayMode = voteDisplayMode,
                            onClick = {
                                onHeaderClick(commentView)
                            },
                            onLongClick = {
                                onHeaderLongClick(commentView)
                            },
                            collapsedCommentsCount = commentView.counts.child_count,
                            isExpanded = isExpanded(commentId),
                            showAvatar = showAvatar,
                        )
                        AnimatedVisibility(
                            visible = isExpanded(commentId) || showCollapsedCommentContent,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Column {
                                CommentBody(
                                    comment = commentView.comment,
                                    viewSource = viewSource,
                                    onClick = { onCommentClick(commentView) },
                                    onLongClick = { v ->
                                        if (v is TextView) {
                                            // Also triggers for long click on links, so we check if link was hit
                                            // Can have selection in viewSource but there are no links there
                                            if (viewSource || (v.selectionStart == -1 && v.selectionEnd == -1)) {
                                                toggleActionBar(commentId)
                                            }
                                        }
                                        true
                                    },
                                )
                                AnimatedVisibility(
                                    visible = showActionBar(commentId),
                                    enter = expandVertically(),
                                    exit = shrinkVertically(),
                                ) {
                                    CommentFooterLine(
                                        commentView = commentView,
                                        admins = admins,
                                        moderators = moderators,
                                        instantScores = instantScores,
                                        onUpvoteClick = {
                                            instantScores =
                                                instantScores.update(VoteType.Upvote)
                                            onUpvoteClick(commentView)
                                        },
                                        onDownvoteClick = {
                                            instantScores =
                                                instantScores.update(VoteType.Downvote)
                                            onDownvoteClick(commentView)
                                        },
                                        onViewSourceClick = {
                                            viewSource = !viewSource
                                        },
                                        onEditCommentClick = onEditCommentClick,
                                        onDeleteCommentClick = onDeleteCommentClick,
                                        onReplyClick = onReplyClick,
                                        onSaveClick = onSaveClick,
                                        onReportClick = onReportClick,
                                        onRemoveClick = onRemoveClick,
                                        onDistinguishClick = onDistinguishClick,
                                        onBanPersonClick = onBanPersonClick,
                                        onBanFromCommunityClick = onBanFromCommunityClick,
                                        onCommentLinkClick = onCommentLinkClick,
                                        onPersonClick = onPersonClick,
                                        onViewVotesClick = onViewVotesClick,
                                        onBlockCreatorClick = onBlockCreatorClick,
                                        onClick = {
                                            toggleExpanded(commentId)
                                        },
                                        onLongClick = {
                                            toggleActionBar(commentId)
                                        },
                                        account = account,
                                        enableDownVotes = enableDownVotes,
                                        viewSource = viewSource,
                                    )
                                }
                            }
                        }
                    }
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

    increaseLazyListIndexTracker()
    item(key = "${commentId}_show_more_children") {
        AnimatedVisibility(
            visible = showMoreChildren,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            ShowMoreChildrenNode(node.depth, commentView, onFetchChildrenClick, isCollapsedByParent || !isExpanded(commentId))
        }
    }

    commentNodeItems(
        nodes = node.children.toList(),
        increaseLazyListIndexTracker = increaseLazyListIndexTracker,
        addToParentIndexes = addToParentIndexes,
        isFlat = isFlat,
        isExpanded = isExpanded,
        toggleExpanded = toggleExpanded,
        toggleActionBar = toggleActionBar,
        onUpvoteClick = onUpvoteClick,
        onDownvoteClick = onDownvoteClick,
        onReplyClick = onReplyClick,
        onSaveClick = onSaveClick,
        onMarkAsReadClick = onMarkAsReadClick,
        onCommentClick = onCommentClick,
        onEditCommentClick = onEditCommentClick,
        onDeleteCommentClick = onDeleteCommentClick,
        onReportClick = onReportClick,
        onRemoveClick = onRemoveClick,
        onDistinguishClick = onDistinguishClick,
        onBanPersonClick = onBanPersonClick,
        onBanFromCommunityClick = onBanFromCommunityClick,
        onCommentLinkClick = onCommentLinkClick,
        onFetchChildrenClick = onFetchChildrenClick,
        onPersonClick = onPersonClick,
        onViewVotesClick = onViewVotesClick,
        onHeaderClick = onHeaderClick,
        onHeaderLongClick = onHeaderLongClick,
        onCommunityClick = onCommunityClick,
        onBlockCreatorClick = onBlockCreatorClick,
        onPostClick = onPostClick,
        account = account,
        showPostAndCommunityContext = showPostAndCommunityContext,
        showCollapsedCommentContent = showCollapsedCommentContent,
        isCollapsedByParent = isCollapsedByParent || !isExpanded(commentId),
        showActionBar = showActionBar,
        enableDownVotes = enableDownVotes,
        showAvatar = showAvatar,
        blurNSFW = blurNSFW,
        voteDisplayMode = voteDisplayMode,
        admins = admins,
        moderators = moderators,
        swipeToActionPreset = swipeToActionPreset,
    )
}

fun LazyListScope.missingCommentNodeItem(
    node: MissingCommentNode,
    admins: List<PersonView>,
    moderators: List<PersonId>?,
    increaseLazyListIndexTracker: () -> Unit,
    addToParentIndexes: () -> Unit,
    isFlat: Boolean,
    isExpanded: (commentId: CommentId) -> Boolean,
    toggleExpanded: (commentId: CommentId) -> Unit,
    toggleActionBar: (commentId: CommentId) -> Unit,
    onUpvoteClick: (commentView: CommentView) -> Unit,
    onDownvoteClick: (commentView: CommentView) -> Unit,
    onReplyClick: (commentView: CommentView) -> Unit,
    onSaveClick: (commentView: CommentView) -> Unit,
    onMarkAsReadClick: (commentView: CommentView) -> Unit,
    onCommentClick: (commentView: CommentView) -> Unit,
    onEditCommentClick: (commentView: CommentView) -> Unit,
    onDeleteCommentClick: (commentView: CommentView) -> Unit,
    onPersonClick: (personId: PersonId) -> Unit,
    onViewVotesClick: (CommentId) -> Unit,
    onHeaderClick: (commentView: CommentView) -> Unit,
    onHeaderLongClick: (commentView: CommentView) -> Unit,
    onCommunityClick: (community: Community) -> Unit,
    onPostClick: (postId: PostId) -> Unit,
    onReportClick: (commentView: CommentView) -> Unit,
    onRemoveClick: (commentView: CommentView) -> Unit,
    onDistinguishClick: (commentView: CommentView) -> Unit,
    onBanPersonClick: (person: Person) -> Unit,
    onBanFromCommunityClick: (banData: BanFromCommunityData) -> Unit,
    onCommentLinkClick: (commentView: CommentView) -> Unit,
    onBlockCreatorClick: (creator: Person) -> Unit,
    onFetchChildrenClick: (commentView: CommentView) -> Unit,
    showCollapsedCommentContent: Boolean,
    showPostAndCommunityContext: Boolean = false,
    account: Account,
    isCollapsedByParent: Boolean,
    showActionBar: (commentId: CommentId) -> Boolean,
    enableDownVotes: Boolean,
    showAvatar: Boolean,
    blurNSFW: BlurNSFW,
    voteDisplayMode: VoteDisplayMode,
    swipeToActionPreset: SwipeToActionPreset,
) {
    val commentId = node.missingCommentView.commentId

    val offset = calculateCommentOffset(node.depth) // The ones with a border on
    val offset2 =
        if (node.depth == 0) {
            MEDIUM_PADDING
        } else {
            XXL_PADDING
        }

    if (node.depth == 0) {
        addToParentIndexes()
    }

    increaseLazyListIndexTracker()
    // TODO Needs a contentType
    // possibly "contentNodeItemL${node.depth}"
    item(key = commentId) {
        val backgroundColor = MaterialTheme.colorScheme.background
        val borderColor = calculateBorderColor(backgroundColor, node.depth)
        val border = Border(BORDER_WIDTH, borderColor)

        AnimatedVisibility(
            visible = !isCollapsedByParent,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(
                            start = offset,
                        ).border(start = border),
            ) {
                Column(
                    modifier =
                        Modifier.padding(
                            start = offset2,
                            end = MEDIUM_PADDING,
                        ),
                ) {
                    AnimatedVisibility(
                        visible = isExpanded(commentId) || showCollapsedCommentContent,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Text(
                            text = stringResource(id = R.string.comment_gone),
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(vertical = SMALL_PADDING),
                        )
                    }
                }
            }
        }
    }

    increaseLazyListIndexTracker()

    commentNodeItems(
        nodes = node.children.toList(),
        admins = admins,
        moderators = moderators,
        increaseLazyListIndexTracker = increaseLazyListIndexTracker,
        addToParentIndexes = addToParentIndexes,
        isFlat = isFlat,
        isExpanded = isExpanded,
        toggleExpanded = toggleExpanded,
        toggleActionBar = toggleActionBar,
        onUpvoteClick = onUpvoteClick,
        onDownvoteClick = onDownvoteClick,
        onReplyClick = onReplyClick,
        onSaveClick = onSaveClick,
        onMarkAsReadClick = onMarkAsReadClick,
        onCommentClick = onCommentClick,
        onEditCommentClick = onEditCommentClick,
        onDeleteCommentClick = onDeleteCommentClick,
        onReportClick = onReportClick,
        onRemoveClick = onRemoveClick,
        onDistinguishClick = onDistinguishClick,
        onBanPersonClick = onBanPersonClick,
        onBanFromCommunityClick = onBanFromCommunityClick,
        onCommentLinkClick = onCommentLinkClick,
        onFetchChildrenClick = onFetchChildrenClick,
        onPersonClick = onPersonClick,
        onViewVotesClick = onViewVotesClick,
        onHeaderClick = onHeaderClick,
        onHeaderLongClick = onHeaderLongClick,
        onCommunityClick = onCommunityClick,
        onBlockCreatorClick = onBlockCreatorClick,
        onPostClick = onPostClick,
        account = account,
        showPostAndCommunityContext = showPostAndCommunityContext,
        showCollapsedCommentContent = showCollapsedCommentContent,
        isCollapsedByParent = isCollapsedByParent || !isExpanded(commentId),
        showActionBar = showActionBar,
        enableDownVotes = enableDownVotes,
        showAvatar = showAvatar,
        blurNSFW = blurNSFW,
        voteDisplayMode = voteDisplayMode,
        swipeToActionPreset = swipeToActionPreset,
    )
}

@Composable
private fun ShowMoreChildrenNode(
    depth: Int,
    commentView: CommentView,
    onFetchChildrenClick: (commentView: CommentView) -> Unit,
    isCollapsedByParent: Boolean,
) {
    val newDepth = depth + 1

    val offset = calculateCommentOffset(newDepth) // The ones with a border on
    val offset2 =
        if (newDepth == 0) {
            MEDIUM_PADDING
        } else {
            XXL_PADDING
        }

    val backgroundColor = MaterialTheme.colorScheme.background
    val borderColor = calculateBorderColor(backgroundColor, newDepth)
    val border = Border(BORDER_WIDTH, borderColor)

    AnimatedVisibility(
        visible = !isCollapsedByParent,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(
                        start = offset,
                    ),
        ) {
            Column(
                modifier = Modifier.border(start = border),
            ) {
                Column(
                    modifier = Modifier.padding(start = offset2, end = MEDIUM_PADDING),
                ) {
                    ShowMoreChildren(
                        commentView = commentView,
                        onFetchChildrenClick = onFetchChildrenClick,
                    )
                }
            }
        }
    }
}

@Composable
fun PostAndCommunityContextHeader(
    post: Post,
    community: Community,
    onCommunityClick: (community: Community) -> Unit,
    onPostClick: (postId: PostId) -> Unit,
    blurNSFW: BlurNSFW,
    showAvatar: Boolean,
) {
    Column(
        modifier = Modifier.padding(top = LARGE_PADDING),
    ) {
        Text(
            text = post.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable { onPostClick(post.id) },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(R.string.comment_node_in), color = MaterialTheme.colorScheme.onBackground.muted)
            CommunityLink(
                community = community,
                onClick = onCommunityClick,
                showDefaultIcon = false,
                blurNSFW = blurNSFW,
                showAvatar = showAvatar,
            )
        }
    }
}

@Preview
@Composable
fun PostAndCommunityContextHeaderPreview() {
    PostAndCommunityContextHeader(
        post = samplePost,
        community = sampleCommunity,
        onCommunityClick = {},
        onPostClick = {},
        blurNSFW = BlurNSFW.NSFW,
        showAvatar = true,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentFooterLine(
    commentView: CommentView,
    admins: List<PersonView>,
    moderators: List<PersonId>?,
    enableDownVotes: Boolean,
    instantScores: InstantScores,
    onUpvoteClick: () -> Unit,
    onDownvoteClick: () -> Unit,
    onReplyClick: (commentView: CommentView) -> Unit,
    onSaveClick: (commentView: CommentView) -> Unit,
    onViewSourceClick: () -> Unit,
    onEditCommentClick: (commentView: CommentView) -> Unit,
    onDeleteCommentClick: (commentView: CommentView) -> Unit,
    onReportClick: (commentView: CommentView) -> Unit,
    onRemoveClick: (commentView: CommentView) -> Unit,
    onDistinguishClick: (commentView: CommentView) -> Unit,
    onBanPersonClick: (person: Person) -> Unit,
    onBanFromCommunityClick: (banData: BanFromCommunityData) -> Unit,
    onCommentLinkClick: (commentView: CommentView) -> Unit,
    onBlockCreatorClick: (creator: Person) -> Unit,
    onPersonClick: (personId: PersonId) -> Unit,
    onViewVotesClick: (CommentId) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    account: Account,
    viewSource: Boolean,
) {
    var showMoreOptions by remember { mutableStateOf(false) }

    val amMod = remember(moderators) {
        amMod(
            moderators = moderators,
            myId = account.id,
        )
    }

    val canMod = remember(admins) {
        canMod(
            creatorId = commentView.comment.creator_id,
            admins = admins,
            moderators = moderators,
            myId = account.id,
        )
    }

    if (showMoreOptions) {
        CommentOptionsDropdown(
            commentView = commentView,
            onDismissRequest = { showMoreOptions = false },
            onViewSourceClick = onViewSourceClick,
            onEditCommentClick = onEditCommentClick,
            onDeleteCommentClick = onDeleteCommentClick,
            onReportClick = onReportClick,
            onRemoveClick = onRemoveClick,
            onDistinguishClick = onDistinguishClick,
            onBanPersonClick = onBanPersonClick,
            onBanFromCommunityClick = onBanFromCommunityClick,
            onBlockCreatorClick = onBlockCreatorClick,
            onCommentLinkClick = onCommentLinkClick,
            onPersonClick = onPersonClick,
            onViewVotesClick = onViewVotesClick,
            isCreator = account.id == commentView.creator.id,
            canMod = canMod,
            amMod = amMod,
            amAdmin = account.isAdmin,
            viewSource = viewSource,
        )
    }

    Row(
        horizontalArrangement = Arrangement.End,
        modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(top = MEDIUM_PADDING),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(XXL_PADDING),
        ) {
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
            ActionBarButton(
                icon = Icons.AutoMirrored.Outlined.Comment,
                onClick = { onReplyClick(commentView) },
                contentDescription = stringResource(R.string.commentFooter_reply),
                account = account,
            )
            ActionBarButton(
                icon =
                    if (commentView.saved) {
                        Icons.Filled.Bookmark
                    } else {
                        Icons.Outlined.BookmarkBorder
                    },
                contentDescription =
                    if (commentView.saved) {
                        stringResource(R.string.removeBookmark)
                    } else {
                        stringResource(R.string.addBookmark)
                    },
                onClick = { onSaveClick(commentView) },
                contentColor =
                    if (commentView.saved) {
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
            )
        }
    }
}

@Preview
@Composable
fun CommentNodesPreview() {
    MarkdownHelper.init(LocalContext.current)
    val comments =
        listOf(
            sampleSecondReplyCommentView,
            sampleCommentView,
            sampleReplyCommentView,
        )
    val tree = buildCommentsTree(comments, null)
    CommentNodes(
        nodes = tree,
        admins = emptyList(),
        moderators = emptyList(),
        increaseLazyListIndexTracker = {},
        addToParentIndexes = {},
        isFlat = false,
        isExpanded = { _ -> true },
        toggleExpanded = {},
        toggleActionBar = {},
        onUpvoteClick = {},
        onDownvoteClick = {},
        onReplyClick = {},
        onFetchChildrenClick = {},
        onSaveClick = {},
        onMarkAsReadClick = {},
        onCommentClick = {},
        onEditCommentClick = {},
        onDeleteCommentClick = {},
        onReportClick = {},
        onRemoveClick = {},
        onDistinguishClick = {},
        onBanPersonClick = {},
        onBanFromCommunityClick = {},
        onCommentLinkClick = {},
        onPersonClick = {},
        onViewVotesClick = {},
        onHeaderClick = {},
        onHeaderLongClick = {},
        onCommunityClick = {},
        onBlockCreatorClick = {},
        onPostClick = {},
        listState = rememberLazyListState(),
        isCollapsedByParent = false,
        showCollapsedCommentContent = false,
        showActionBar = { _ -> true },
        enableDownVotes = true,
        showAvatar = true,
        blurNSFW = BlurNSFW.NSFW,
        account = AnonAccount,
        voteDisplayMode = VoteDisplayMode.Full,
        swipeToActionPreset = SwipeToActionPreset.TwoSides,
    )
}

@Composable
fun ShowMoreChildren(
    commentView: CommentView,
    onFetchChildrenClick: (commentView: CommentView) -> Unit,
) {
    TextButton(
        content = {
            Text(stringResource(R.string.comment_node_more_replies, commentView.counts.child_count))
        },
        onClick = { onFetchChildrenClick(commentView) },
    )
}

@Composable
@Preview
fun ShowMoreChildrenPreview() {
    ShowMoreChildren(
        commentView = sampleCommentView,
        onFetchChildrenClick = {},
    )
}

fun calculateBorderColor(
    defaultBackground: Color,
    depth: Int,
): Color {
    return if (depth == 0) {
        defaultBackground
    } else {
        colorList[depth.minus(1).mod(colorList.size)]
    }
}

@Composable
fun ShowCommentContextButtons(
    postId: PostId,
    commentParentId: CommentId?,
    showContextButton: Boolean,
    onPostClick: (postId: PostId) -> Unit,
    onCommentClick: (commentId: CommentId) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
        modifier = Modifier.padding(MEDIUM_PADDING),
    ) {
        OutlinedButton(
            content = {
                Text(stringResource(R.string.comment_node_view_post))
            },
            onClick = { onPostClick(postId) },
        )
        if (showContextButton && commentParentId != null) {
            OutlinedButton(
                content = {
                    Text(stringResource(R.string.comment_node_view_context))
                },
                onClick = { onCommentClick(commentParentId) },
            )
        }
    }
}

@Composable
@Preview
fun ShowCommentContextButtonsPreview() {
    ShowCommentContextButtons(
        postId = 0,
        commentParentId = 0,
        showContextButton = true,
        onPostClick = {},
        onCommentClick = {},
    )
}
