package com.jerboa.ui.components.post

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import arrow.core.Either
import com.jerboa.JerboaAppState
import com.jerboa.PostViewMode
import com.jerboa.R
import com.jerboa.api.ApiState
import com.jerboa.buildCommentsTree
import com.jerboa.datatypes.BanFromCommunityData
import com.jerboa.datatypes.getLocalizedCommentSortTypeName
import com.jerboa.db.entity.isAnon
import com.jerboa.feat.BlurNSFW
import com.jerboa.feat.PostActionBarMode
import com.jerboa.feat.SwipeToActionPreset
import com.jerboa.feat.VoteType
import com.jerboa.feat.doIfReadyElseDisplayInfo
import com.jerboa.feat.newVote
import com.jerboa.getCommentParentId
import com.jerboa.getDepthFromComment
import com.jerboa.model.AccountViewModel
import com.jerboa.model.PostViewModel
import com.jerboa.model.ReplyItem
import com.jerboa.model.SiteViewModel
import com.jerboa.scrollToNextParentComment
import com.jerboa.scrollToPreviousParentComment
import com.jerboa.ui.components.ban.BanFromCommunityReturn
import com.jerboa.ui.components.ban.BanPersonReturn
import com.jerboa.ui.components.comment.ShowCommentContextButtons
import com.jerboa.ui.components.comment.commentNodeItems
import com.jerboa.ui.components.comment.edit.CommentEditReturn
import com.jerboa.ui.components.comment.reply.CommentReplyReturn
import com.jerboa.ui.components.common.ApiErrorText
import com.jerboa.ui.components.common.CommentNavigationBottomAppBar
import com.jerboa.ui.components.common.CommentSortOptionsDropdown
import com.jerboa.ui.components.common.JerboaPullRefreshIndicator
import com.jerboa.ui.components.common.JerboaSnackbarHost
import com.jerboa.ui.components.common.LoadingBar
import com.jerboa.ui.components.common.apiErrorToast
import com.jerboa.ui.components.common.getCurrentAccount
import com.jerboa.ui.components.common.isLoading
import com.jerboa.ui.components.common.isRefreshing
import com.jerboa.ui.components.common.simpleVerticalScrollbar
import com.jerboa.ui.components.post.edit.PostEditReturn
import com.jerboa.ui.components.remove.comment.CommentRemoveReturn
import com.jerboa.ui.components.remove.post.PostRemoveReturn
import it.vercruysse.lemmyapi.dto.CommentSortType
import it.vercruysse.lemmyapi.v0x19.datatypes.BlockPerson
import it.vercruysse.lemmyapi.v0x19.datatypes.CommentId
import it.vercruysse.lemmyapi.v0x19.datatypes.CommentView
import it.vercruysse.lemmyapi.v0x19.datatypes.CreateCommentLike
import it.vercruysse.lemmyapi.v0x19.datatypes.CreatePostLike
import it.vercruysse.lemmyapi.v0x19.datatypes.DeleteComment
import it.vercruysse.lemmyapi.v0x19.datatypes.DeletePost
import it.vercruysse.lemmyapi.v0x19.datatypes.DistinguishComment
import it.vercruysse.lemmyapi.v0x19.datatypes.FeaturePost
import it.vercruysse.lemmyapi.v0x19.datatypes.LockPost
import it.vercruysse.lemmyapi.v0x19.datatypes.PersonView
import it.vercruysse.lemmyapi.v0x19.datatypes.PostId
import it.vercruysse.lemmyapi.v0x19.datatypes.PostView
import it.vercruysse.lemmyapi.v0x19.datatypes.SaveComment
import it.vercruysse.lemmyapi.v0x19.datatypes.SavePost

object PostViewReturn {
    const val POST_VIEW = "post-view::return(post-view)"
}

@Composable
fun CommentsHeaderTitle(selectedSortType: CommentSortType) {
    val ctx = LocalContext.current
    Column {
        Text(
            text = stringResource(R.string.post_activity_comments),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = getLocalizedCommentSortTypeName(ctx, selectedSortType),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
@Composable
fun PostActivity(
    id: Either<PostId, CommentId>,
    siteViewModel: SiteViewModel,
    accountViewModel: AccountViewModel,
    appState: JerboaAppState,
    useCustomTabs: Boolean,
    usePrivateTabs: Boolean,
    showCollapsedCommentContent: Boolean,
    showActionBarByDefault: Boolean,
    showVotingArrowsInListView: Boolean,
    showParentCommentNavigationButtons: Boolean,
    navigateParentCommentsWithVolumeButtons: Boolean,
    blurNSFW: BlurNSFW,
    showPostLinkPreview: Boolean,
    postActionBarMode: PostActionBarMode,
    swipeToActionPreset: SwipeToActionPreset,
) {
    Log.d("jerboa", "got to post activity")

    val ctx = LocalContext.current

    val account = getCurrentAccount(accountViewModel = accountViewModel)

    val postViewModel: PostViewModel = viewModel(factory = PostViewModel.Companion.Factory(id))

    appState.ConsumeReturn<PostView>(PostEditReturn.POST_VIEW, postViewModel::updatePost)
    appState.ConsumeReturn<PostView>(PostRemoveReturn.POST_VIEW, postViewModel::updatePost)
    appState.ConsumeReturn<CommentView>(CommentReplyReturn.COMMENT_VIEW, postViewModel::appendComment)
    appState.ConsumeReturn<CommentView>(CommentEditReturn.COMMENT_VIEW, postViewModel::updateComment)
    appState.ConsumeReturn<CommentView>(CommentRemoveReturn.COMMENT_VIEW, postViewModel::updateComment)
    appState.ConsumeReturn<PersonView>(BanPersonReturn.PERSON_VIEW, postViewModel::updateBanned)
    appState.ConsumeReturn<BanFromCommunityData>(
        BanFromCommunityReturn.BAN_DATA_VIEW,
        postViewModel::updateBannedFromCommunity,
    )

    val onClickSortType = { commentSortType: CommentSortType ->
        postViewModel.updateSortType(commentSortType)
        postViewModel.getData()
    }

    val selectedSortType = postViewModel.sortType

    // Holds expanded comment ids
    val unExpandedComments = postViewModel.unExpandedComments
    val commentsWithToggledActionBar = postViewModel.commentsWithToggledActionBar
    var showSortOptions by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val listState = rememberLazyListState()
    var lazyListIndexTracker: Int
    val parentListStateIndexes = remember { mutableListOf<Int>() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = postViewModel.postRes.isRefreshing(),
            onRefresh = {
                postViewModel.getData(ApiState.Refreshing)
            },
        )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        snackbarHost = { JerboaSnackbarHost(snackbarHostState) },
        modifier =
            Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .semantics { testTagsAsResourceId = true }
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    if (navigateParentCommentsWithVolumeButtons) {
                        when (keyEvent.key) {
                            Key.VolumeUp -> {
                                scrollToPreviousParentComment(scope, parentListStateIndexes, listState)
                                true
                            }

                            Key.VolumeDown -> {
                                scrollToNextParentComment(scope, parentListStateIndexes, listState)
                                true
                            }

                            else -> {
                                false
                            }
                        }
                    } else {
                        false
                    }
                },
        bottomBar = {
            if (showParentCommentNavigationButtons) {
                CommentNavigationBottomAppBar(
                    scope,
                    parentListStateIndexes,
                    listState,
                )
            }
        },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        CommentsHeaderTitle(
                            selectedSortType = selectedSortType,
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            modifier = Modifier.testTag("jerboa:back"),
                            onClick = appState::navigateUp,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.topAppBar_back),
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = {
                                showSortOptions = true
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Sort,
                                    contentDescription = stringResource(R.string.selectSort),
                                )
                            }

                            CommentSortOptionsDropdown(
                                expanded = showSortOptions,
                                selectedSortType = selectedSortType,
                                onDismissRequest = { showSortOptions = false },
                                onClickSortType = onClickSortType,
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        content = { padding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState),
            ) {
                parentListStateIndexes.clear()
                lazyListIndexTracker = 2
                JerboaPullRefreshIndicator(
                    postViewModel.postRes.isRefreshing(),
                    pullRefreshState,
                    // zIndex needed bc some elements of a post get drawn above it.
                    Modifier
                        .padding(padding)
                        .align(Alignment.TopCenter)
                        .zIndex(100f),
                )
                when (val postRes = postViewModel.postRes) {
                    is ApiState.Loading ->
                        LoadingBar(padding)

                    is ApiState.Failure -> ApiErrorText(postRes.msg, padding)
                    is ApiState.Success -> {
                        val postView = postRes.data.post_view
                        val moderators = remember(postRes) { postRes.data.moderators.map { it.moderator.id } }

                        if (!account.isAnon()) appState.addReturn(PostViewReturn.POST_VIEW, postView.copy(read = true))
                        LazyColumn(
                            state = listState,
                            modifier =
                                Modifier
                                    .padding(top = padding.calculateTopPadding())
                                    .simpleVerticalScrollbar(listState)
                                    .testTag("jerboa:comments"),
                        ) {
                            item(key = "${postView.post.id}_listing", "post_listing") {
                                PostListing(
                                    postView = postView,
                                    admins = siteViewModel.admins(),
                                    moderators = moderators,
                                    onUpvoteClick = { pv ->
                                        account.doIfReadyElseDisplayInfo(
                                            appState,
                                            ctx,
                                            snackbarHostState,
                                            scope,
                                            siteViewModel,
                                            accountViewModel,
                                        ) {
                                            postViewModel.likePost(
                                                CreatePostLike(
                                                    post_id = pv.post.id,
                                                    score =
                                                        newVote(
                                                            postView.my_vote,
                                                            VoteType.Upvote,
                                                        ).toLong(),
                                                ),
                                            )
                                        }
                                    },
                                    onDownvoteClick = { pv ->
                                        account.doIfReadyElseDisplayInfo(
                                            appState,
                                            ctx,
                                            snackbarHostState,
                                            scope,
                                            siteViewModel,
                                            accountViewModel,
                                        ) {
                                            postViewModel.likePost(
                                                CreatePostLike(
                                                    post_id = pv.post.id,
                                                    score = newVote(
                                                        postView.my_vote,
                                                        VoteType.Downvote,
                                                    ).toLong(),
                                                ),
                                            )
                                        }
                                    },
                                    onReplyClick = { pv ->
                                        appState.toCommentReply(
                                            replyItem = ReplyItem.PostItem(pv),
                                        )
                                    },
                                    onPostClick = {},
                                    onSaveClick = { pv ->
                                        account.doIfReadyElseDisplayInfo(
                                            appState,
                                            ctx,
                                            snackbarHostState,
                                            scope,
                                            siteViewModel,
                                            accountViewModel,
                                        ) {
                                            postViewModel.savePost(
                                                SavePost(
                                                    post_id = pv.post.id,
                                                    save = !pv.saved,
                                                ),
                                            )
                                        }
                                    },
                                    onCommunityClick = { community ->
                                        appState.toCommunity(id = community.id)
                                    },
                                    onEditPostClick = { pv ->
                                        appState.toPostEdit(
                                            postView = pv,
                                        )
                                    },
                                    onDeletePostClick = { pv ->
                                        account.doIfReadyElseDisplayInfo(
                                            appState,
                                            ctx,
                                            snackbarHostState,
                                            scope,
                                            siteViewModel,
                                            accountViewModel,
                                        ) {
                                            postViewModel.deletePost(
                                                DeletePost(
                                                    post_id = pv.post.id,
                                                    deleted = !pv.post.deleted,
                                                ),
                                            )
                                        }
                                    },
                                    onReportClick = { pv ->
                                        appState.toPostReport(id = pv.post.id)
                                    },
                                    onRemoveClick = { pv ->
                                        appState.toPostRemove(post = pv.post)
                                    },
                                    onBanPersonClick = { p ->
                                        appState.toBanPerson(person = p)
                                    },
                                    onBanFromCommunityClick = { d ->
                                        appState.toBanFromCommunity(banData = d)
                                    },
                                    onLockPostClick = { pv ->
                                        account.doIfReadyElseDisplayInfo(
                                            appState,
                                            ctx,
                                            snackbarHostState,
                                            scope,
                                            siteViewModel,
                                            accountViewModel,
                                        ) {
                                            postViewModel.lockPost(
                                                LockPost(
                                                    post_id = pv.post.id,
                                                    locked = !pv.post.locked,
                                                ),
                                            )
                                        }
                                    },
                                    onFeaturePostClick = { data ->
                                        account.doIfReadyElseDisplayInfo(
                                            appState,
                                            ctx,
                                            snackbarHostState,
                                            scope,
                                            siteViewModel,
                                            accountViewModel,
                                        ) {
                                            postViewModel.featurePost(
                                                FeaturePost(
                                                    post_id = data.post.id,
                                                    featured = !data.featured,
                                                    feature_type = data.type,
                                                ),
                                            )
                                        }
                                    },
                                    onViewVotesClick = appState::toPostLikes,
                                    onPersonClick = appState::toProfile,
                                    // Do nothing
                                    showReply = true,
                                    fullBody = true,
                                    account = account,
                                    postViewMode = PostViewMode.Card,
                                    enableDownVotes = siteViewModel.enableDownvotes(),
                                    showAvatar = siteViewModel.showAvatar(),
                                    showVotingArrowsInListView = showVotingArrowsInListView,
                                    useCustomTabs = useCustomTabs,
                                    usePrivateTabs = usePrivateTabs,
                                    blurNSFW = blurNSFW,
                                    appState = appState,
                                    showPostLinkPreview = showPostLinkPreview,
                                    showIfRead = false,
                                    voteDisplayMode = siteViewModel.voteDisplayMode(),
                                    postActionBarMode = postActionBarMode,
                                    swipeToActionPreset = swipeToActionPreset,
                                )
                            }

                            if (postViewModel.commentsRes.isLoading()) {
                                item(contentType = "loadingbar") {
                                    LoadingBar()
                                }
                            }

                            item(contentType = "horizontalDivider") {
                                HorizontalDivider()
                            }

                            when (val commentsRes = postViewModel.commentsRes) {
                                is ApiState.Failure ->
                                    item(key = "error") {
                                        apiErrorToast(
                                            ctx,
                                            commentsRes.msg,
                                        )
                                    }

                                is ApiState.Holder -> {
                                    val commentTree =
                                        buildCommentsTree(
                                            commentsRes.data.comments,
                                            id.fold(
                                                { null },
                                                { it },
                                            ),
                                        )

                                    val toggleExpanded: (CommentId) -> Unit = { commentId: CommentId ->
                                        if (unExpandedComments.contains(commentId)) {
                                            unExpandedComments.remove(commentId)
                                        } else {
                                            unExpandedComments.add(commentId)
                                        }
                                    }

                                    val toggleActionBar: (CommentId) -> Unit = { commentId: CommentId ->
                                        if (commentsWithToggledActionBar.contains(commentId)) {
                                            commentsWithToggledActionBar.remove(commentId)
                                        } else {
                                            commentsWithToggledActionBar.add(commentId)
                                        }
                                    }

                                    item(key = "${postView.post.id}_is_comment_view", contentType = "contextButtons") {
                                        if (postViewModel.isCommentView()) {
                                            val firstCommentNodeData = commentTree.firstOrNull()

                                            val firstCommentPath = firstCommentNodeData?.getPath()

                                            val hasParent =
                                                firstCommentPath != null && getDepthFromComment(firstCommentPath) > 0

                                            val commentParentId = firstCommentPath?.let(::getCommentParentId)

                                            ShowCommentContextButtons(
                                                postView.post.id,
                                                commentParentId = commentParentId,
                                                showContextButton = hasParent,
                                                onPostClick = appState::toPost,
                                                onCommentClick = appState::toComment,
                                            )
                                        }
                                    }

                                    commentNodeItems(
                                        nodes = commentTree,
                                        admins = siteViewModel.admins(),
                                        moderators = moderators,
                                        increaseLazyListIndexTracker = {
                                            lazyListIndexTracker++
                                        },
                                        addToParentIndexes = {
                                            parentListStateIndexes.add(lazyListIndexTracker)
                                        },
                                        isFlat = false,
                                        isExpanded = { commentId ->
                                            !unExpandedComments.contains(
                                                commentId,
                                            )
                                        },
                                        toggleExpanded = toggleExpanded,
                                        toggleActionBar = toggleActionBar,
                                        onMarkAsReadClick = {},
                                        onCommentClick = { commentView -> toggleExpanded(commentView.comment.id) },
                                        onUpvoteClick = { cv ->
                                            account.doIfReadyElseDisplayInfo(
                                                appState,
                                                ctx,
                                                snackbarHostState,
                                                scope,
                                                siteViewModel,
                                                accountViewModel,
                                            ) {
                                                postViewModel.likeComment(
                                                    CreateCommentLike(
                                                        comment_id = cv.comment.id,
                                                        score =
                                                            newVote(
                                                                cv.my_vote,
                                                                VoteType.Upvote,
                                                            ).toLong(),
                                                    ),
                                                )
                                            }
                                        },
                                        onDownvoteClick = { cv ->
                                            account.doIfReadyElseDisplayInfo(
                                                appState,
                                                ctx,
                                                snackbarHostState,
                                                scope,
                                                siteViewModel,
                                                accountViewModel,
                                            ) {
                                                postViewModel.likeComment(
                                                    CreateCommentLike(
                                                        comment_id = cv.comment.id,
                                                        score = newVote(
                                                            cv.my_vote,
                                                            VoteType.Downvote,
                                                        ).toLong(),
                                                    ),
                                                )
                                            }
                                        },
                                        onReplyClick = { cv ->
                                            appState.toCommentReply(
                                                replyItem = ReplyItem.CommentItem(cv),
                                            )
                                        },
                                        onSaveClick = { cv ->
                                            account.doIfReadyElseDisplayInfo(
                                                appState,
                                                ctx,
                                                snackbarHostState,
                                                scope,
                                                siteViewModel,
                                                accountViewModel,
                                            ) {
                                                postViewModel.saveComment(
                                                    SaveComment(
                                                        comment_id = cv.comment.id,
                                                        save = !cv.saved,
                                                    ),
                                                )
                                            }
                                        },
                                        onPersonClick = appState::toProfile,
                                        onViewVotesClick = appState::toCommentLikes,
                                        onHeaderClick = { commentView -> toggleExpanded(commentView.comment.id) },
                                        onHeaderLongClick = { commentView -> toggleActionBar(commentView.comment.id) },
                                        onEditCommentClick = { cv ->
                                            appState.toCommentEdit(
                                                commentView = cv,
                                            )
                                        },
                                        onDeleteCommentClick = { cv ->
                                            account.doIfReadyElseDisplayInfo(
                                                appState,
                                                ctx,
                                                snackbarHostState,
                                                scope,
                                                siteViewModel,
                                                accountViewModel,
                                            ) {
                                                postViewModel.deleteComment(
                                                    DeleteComment(
                                                        comment_id = cv.comment.id,
                                                        deleted = !cv.comment.deleted,
                                                    ),
                                                )
                                            }
                                        },
                                        onDistinguishClick = { cv ->
                                            account.doIfReadyElseDisplayInfo(
                                                appState,
                                                ctx,
                                                snackbarHostState,
                                                scope,
                                                siteViewModel,
                                                accountViewModel,
                                            ) {
                                                postViewModel.distinguishComment(
                                                    DistinguishComment(
                                                        comment_id = cv.comment.id,
                                                        distinguished = !cv.comment.distinguished,
                                                    ),
                                                )
                                            }
                                        },
                                        onReportClick = { cv ->
                                            appState.toCommentReport(id = cv.comment.id)
                                        },
                                        onRemoveClick = { cv ->
                                            appState.toCommentRemove(comment = cv.comment)
                                        },
                                        onBanPersonClick = { p ->
                                            appState.toBanPerson(p)
                                        },
                                        onBanFromCommunityClick = { d ->
                                            appState.toBanFromCommunity(banData = d)
                                        },
                                        onCommentLinkClick = { cv ->
                                            appState.toComment(id = cv.comment.id)
                                        },
                                        onFetchChildrenClick = { cv ->
                                            postViewModel.fetchMoreChildren(
                                                commentView = cv,
                                            )
                                        },
                                        onBlockCreatorClick = { person ->
                                            account.doIfReadyElseDisplayInfo(
                                                appState,
                                                ctx,
                                                snackbarHostState,
                                                scope,
                                                siteViewModel,
                                                accountViewModel,
                                            ) {
                                                postViewModel.blockPerson(
                                                    BlockPerson(
                                                        person_id = person.id,
                                                        block = true,
                                                    ),
                                                    ctx,
                                                )
                                            }
                                        },
                                        onCommunityClick = { community ->
                                            appState.toCommunity(id = community.id)
                                        },
                                        // Do nothing
                                        onPostClick = {},
                                        account = account,
                                        enableDownVotes = siteViewModel.enableDownvotes(),
                                        showAvatar = siteViewModel.showAvatar(),
                                        isCollapsedByParent = false,
                                        showCollapsedCommentContent = showCollapsedCommentContent,
                                        showActionBar = { commentId ->
                                            showActionBarByDefault xor
                                                commentsWithToggledActionBar.contains(
                                                    commentId,
                                                )
                                        },
                                        blurNSFW = blurNSFW,
                                        voteDisplayMode = siteViewModel.voteDisplayMode(),
                                        swipeToActionPreset = swipeToActionPreset,
                                    )
                                    item {
                                        Spacer(modifier = Modifier.height(100.dp))
                                    }
                                }

                                else -> {}
                            }
                            if (showParentCommentNavigationButtons) {
                                item {
                                    Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        },
    )
}
