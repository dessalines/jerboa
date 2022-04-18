package com.jerboa.ui.components.comment

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jerboa.CommentNodeData
import com.jerboa.buildCommentsTree
import com.jerboa.datatypes.*
import com.jerboa.db.Account

fun LazyListScope.CommentNodes(
    nodes: List<CommentNodeData>,
    onUpvoteClick: (commentView: CommentView) -> Unit,
    onDownvoteClick: (commentView: CommentView) -> Unit,
    onReplyClick: (commentView: CommentView) -> Unit,
    onSaveClick: (commentView: CommentView) -> Unit,
    onMarkAsReadClick: (commentView: CommentView) -> Unit = {},
    onEditCommentClick: (commentView: CommentView) -> Unit,
    onReportClick: (commentView: CommentView) -> Unit,
    onPersonClick: (personId: Int) -> Unit,
    onCommunityClick: (community: CommunitySafe) -> Unit,
    onBlockCreatorClick: (creator: PersonSafe) -> Unit,
    onPostClick: (postId: Int) -> Unit,
    account: Account? = null,
    moderators: List<CommunityModeratorView>,
    showPostAndCommunityContext: Boolean = false,
    showRead: Boolean = false,
) {
    nodes.forEach { node ->
        CommentNode(
            node = node,
            onUpvoteClick = onUpvoteClick,
            onDownvoteClick = onDownvoteClick,
            onReplyClick = onReplyClick,
            onSaveClick = onSaveClick,
            account = account,
            moderators = moderators,
            onMarkAsReadClick = onMarkAsReadClick,
            onPersonClick = onPersonClick,
            onCommunityClick = onCommunityClick,
            onPostClick = onPostClick,
            onEditCommentClick = onEditCommentClick,
            onReportClick = onReportClick,
            onBlockCreatorClick = onBlockCreatorClick,
            showPostAndCommunityContext = showPostAndCommunityContext,
            showRead = showRead,
        )
    }
}

@Preview
@Composable
fun CommentNodesPreview() {
    val comments = listOf(
        sampleSecondCommentReplyView, sampleCommentReplyView, sampleCommentView
    )
    val tree = buildCommentsTree(comments, SortType.Hot)
    LazyColumn {
        CommentNodes(
            nodes = tree,
            moderators = listOf(),
            onCommunityClick = {},
            onDownvoteClick = {},
            onEditCommentClick = {},
            onMarkAsReadClick = {},
            onPersonClick = {},
            onPostClick = {},
            onReportClick = {},
            onReplyClick = {},
            onSaveClick = {},
            onUpvoteClick = {},
            onBlockCreatorClick = {},
        )
    }
}
