package com.jerboa.ui.components.inbox

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.jerboa.*
import com.jerboa.db.Account
import com.jerboa.db.AccountViewModel
import com.jerboa.ui.components.comment.CommentNodes
import com.jerboa.ui.components.comment.edit.CommentEditViewModel
import com.jerboa.ui.components.comment.edit.commentEditClickWrapper
import com.jerboa.ui.components.comment.reply.CommentReplyViewModel
import com.jerboa.ui.components.comment.reply.commentReplyClickWrapper
import com.jerboa.ui.components.common.BottomAppBarAll
import com.jerboa.ui.components.common.getCurrentAccount
import com.jerboa.ui.components.community.CommunityViewModel
import com.jerboa.ui.components.community.communityClickWrapper
import com.jerboa.ui.components.home.HomeViewModel
import com.jerboa.ui.components.person.PersonProfileViewModel
import com.jerboa.ui.components.person.personClickWrapper
import com.jerboa.ui.components.post.PostViewModel
import com.jerboa.ui.components.post.postClickWrapper
import com.jerboa.ui.components.private_message.PrivateMessage
import com.jerboa.ui.components.report.CreateReportViewModel
import com.jerboa.ui.components.report.commentReportClickWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun InboxActivity(
    navController: NavController,
    inboxViewModel: InboxViewModel,
    homeViewModel: HomeViewModel,
    personProfileViewModel: PersonProfileViewModel,
    postViewModel: PostViewModel,
    communityViewModel: CommunityViewModel,
    accountViewModel: AccountViewModel,
    commentEditViewModel: CommentEditViewModel,
    commentReplyViewModel: CommentReplyViewModel,
    createReportViewModel: CreateReportViewModel,
) {

    Log.d("jerboa", "got to inbox activity")

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val ctx = LocalContext.current
    val accounts by accountViewModel.allAccounts.observeAsState()
    val account = getCurrentAccount(accounts = accounts)
    val unreadCount = homeViewModel.unreadCountResponse?.let { unreadCountTotal(it) }

    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                InboxHeader(
                    unreadCount = unreadCount,
                    navController = navController,
                    selectedUnreadOrAll = unreadOrAllFromBool(inboxViewModel.unreadOnly.value),
                    onClickUnreadOrAll = { unreadOrAll ->
                        account?.also { acct ->
                            inboxViewModel.fetchReplies(
                                account = acct,
                                clear = true,
                                changeUnreadOnly = unreadOrAll == UnreadOrAll.Unread,
                                ctx = ctx,
                            )
                            inboxViewModel.fetchPersonMentions(
                                account = acct,
                                clear = true,
                                changeUnreadOnly = unreadOrAll == UnreadOrAll.Unread,
                                ctx = ctx,
                            )
                            inboxViewModel.fetchPrivateMessages(
                                account = acct,
                                clear = true,
                                changeUnreadOnly = unreadOrAll == UnreadOrAll.Unread,
                                ctx = ctx,
                            )
                        }
                    },
                    onClickMarkAllAsRead = {
                        account?.also { acct ->
                            inboxViewModel.markAllAsRead(
                                account = acct,
                                ctx = ctx,
                            )
                            homeViewModel.markAllAsRead()
                        }
                    }
                )
            },
            content = {
                InboxTabs(
                    padding = it,
                    navController = navController,
                    personProfileViewModel = personProfileViewModel,
                    commentEditViewModel = commentEditViewModel,
                    commentReplyViewModel = commentReplyViewModel,
                    inboxViewModel = inboxViewModel,
                    postViewModel = postViewModel,
                    communityViewModel = communityViewModel,
                    homeViewModel = homeViewModel,
                    createReportViewModel = createReportViewModel,
                    ctx = ctx,
                    account = account,
                    scope = scope,
                )
            },
            bottomBar = {
                BottomAppBarAll(
                    screen = "inbox",
                    unreadCounts = homeViewModel.unreadCountResponse,
                    onClickProfile = {
                        account?.id?.also {
                            personClickWrapper(
                                personProfileViewModel = personProfileViewModel,
                                personId = it,
                                account = account,
                                navController = navController,
                                ctx = ctx,
                            )
                        }
                    },
                    onClickInbox = {
                        inboxClickWrapper(inboxViewModel, account, navController, ctx)
                    },
                    onClickSaved = {
                        account?.id?.also {
                            personClickWrapper(
                                personProfileViewModel = personProfileViewModel,
                                personId = it,
                                account = account,
                                navController = navController,
                                ctx = ctx,
                                saved = true,
                            )
                        }
                    },
                    navController = navController,
                )
            }
        )
    }
}

enum class InboxTab {
    Replies,

    //    Mentions,
    Messages,
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun InboxTabs(
    navController: NavController,
    personProfileViewModel: PersonProfileViewModel,
    inboxViewModel: InboxViewModel,
    communityViewModel: CommunityViewModel,
    homeViewModel: HomeViewModel,
    ctx: Context,
    account: Account?,
    scope: CoroutineScope,
    postViewModel: PostViewModel,
    commentEditViewModel: CommentEditViewModel,
    commentReplyViewModel: CommentReplyViewModel,
    createReportViewModel: CreateReportViewModel,
    padding: PaddingValues,
) {
    val tabTitles = InboxTab.values().map { it.toString() }

    val pagerState = rememberPagerState()

    Column(
        modifier = Modifier.padding(padding)
    ) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(
                        pagerState,
                        tabPositions
                    )
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = title) }
                )
            }
        }
        if (inboxViewModel.loading.value) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        HorizontalPager(
            count = tabTitles.size,
            state = pagerState,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxSize()
        ) { tabIndex ->
            when (tabIndex) {
                InboxTab.Replies.ordinal -> {

                    val nodes = commentsToFlatNodes(inboxViewModel.replies)

                    val listState = rememberLazyListState()
                    val loading = inboxViewModel.loading.value &&
                        inboxViewModel.page.value == 1 &&
                        inboxViewModel.replies.isNotEmpty()

                    // observer when reached end of list
                    val endOfListReached by remember {
                        derivedStateOf {
                            listState.isScrolledToEnd()
                        }
                    }

                    // act when end of list reached
                    if (endOfListReached) {
                        LaunchedEffect(endOfListReached) {
                            account?.also { acct ->
                                if (inboxViewModel.replies.size > 0) {
                                    inboxViewModel.fetchReplies(
                                        account = acct,
                                        nextPage = true,
                                        ctx = ctx,
                                    )
                                }
                            }
                        }
                    }

                    SwipeRefresh(
                        state = rememberSwipeRefreshState(loading),
                        onRefresh = {
                            account?.also { acct ->
                                inboxViewModel.fetchReplies(
                                    account = acct,
                                    clear = true,
                                    ctx = ctx,
                                )
                            }
                        },
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                            // .simpleVerticalScrollbar(listState)
                        ) {
                            CommentNodes(
                                nodes = nodes,
                                onUpvoteClick = { commentView ->
                                    account?.also { acct ->
                                        inboxViewModel.likeComment(
                                            commentView = commentView,
                                            voteType = VoteType.Upvote,
                                            account = acct,
                                            ctx = ctx,
                                        )
                                    }
                                },
                                onDownvoteClick = { commentView ->
                                    account?.also { acct ->
                                        inboxViewModel.likeComment(
                                            commentView = commentView,
                                            voteType = VoteType.Downvote,
                                            account = acct,
                                            ctx = ctx,
                                        )
                                    }
                                },
                                onReplyClick = { commentView ->
                                    commentReplyClickWrapper(
                                        commentReplyViewModel = commentReplyViewModel,
                                        parentCommentView = commentView,
                                        postId = commentView.post.id,
                                        navController = navController,
                                    )
                                },
                                onEditCommentClick = { commentView ->
                                    commentEditClickWrapper(
                                        commentEditViewModel,
                                        commentView,
                                        navController,
                                    )
                                },
                                onReportClick = { commentView ->
                                    commentReportClickWrapper(
                                        createReportViewModel,
                                        commentView.comment.id,
                                        navController,
                                    )
                                },
                                onSaveClick = { commentView ->
                                    account?.also { acct ->
                                        inboxViewModel.saveComment(
                                            commentView = commentView,
                                            account = acct,
                                            ctx = ctx,
                                        )
                                    }
                                },
                                onBlockCreatorClick = {
                                    account?.also { acct ->
                                        inboxViewModel.blockCreator(
                                            creator = it,
                                            account = acct,
                                            ctx = ctx,
                                        )
                                    }
                                },
                                onMarkAsReadClick = { commentView ->
                                    account?.also { acct ->
                                        inboxViewModel.markReplyAsRead(
                                            commentView = commentView,
                                            account = acct,
                                            ctx = ctx,
                                        )
                                        homeViewModel.updateUnreads(commentView)
                                    }
                                },
                                onPersonClick = { personId ->
                                    personClickWrapper(
                                        personProfileViewModel,
                                        personId,
                                        account,
                                        navController,
                                        ctx
                                    )
                                },
                                onCommunityClick = { community ->
                                    communityClickWrapper(
                                        communityViewModel = communityViewModel,
                                        communityId = community.id,
                                        account = account,
                                        navController = navController,
                                        ctx = ctx,
                                    )
                                },
                                onPostClick = { postId ->
                                    postClickWrapper(
                                        postViewModel = postViewModel,
                                        postId = postId,
                                        account = account,
                                        navController = navController,
                                        ctx = ctx,
                                    )
                                },
                                showPostAndCommunityContext = true,
                                showRead = true,
                                account = account,
                                moderators = listOf()
                            )
                        }
                    }
                }

//                InboxTab.Mentions.ordinal -> {
//                    // TODO Need to do a whole type of its own here
//                }
                InboxTab.Messages.ordinal -> {

                    val listState = rememberLazyListState()
                    val loading = inboxViewModel.loading.value &&
                        inboxViewModel.page.value == 1 &&
                        inboxViewModel.messages.isNotEmpty()

                    // observer when reached end of list
                    val endOfListReached by remember {
                        derivedStateOf {
                            listState.isScrolledToEnd()
                        }
                    }

                    // act when end of list reached
                    if (endOfListReached) {
                        LaunchedEffect(endOfListReached) {
                            account?.also { acct ->
                                if (inboxViewModel.messages.size > 0) {
                                    inboxViewModel.fetchPrivateMessages(
                                        account = acct,
                                        nextPage = true,
                                        ctx = ctx,
                                    )
                                }
                            }
                        }
                    }

                    SwipeRefresh(
                        state = rememberSwipeRefreshState(loading),
                        onRefresh = {
                            account?.also { acct ->
                                inboxViewModel.fetchPrivateMessages(
                                    account = acct,
                                    clear = true,
                                    ctx = ctx,
                                )
                            }
                        },
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                            // .simpleVerticalScrollbar(listState)
                        ) {
                            items(inboxViewModel.messages) { message ->
                                account?.also { acct ->
                                    PrivateMessage(
                                        myPersonId = acct.id,
                                        privateMessageView = message,
                                        onReplyClick = { privateMessageView ->
                                            inboxViewModel.replyToPrivateMessageView =
                                                privateMessageView
                                            navController.navigate("privateMessageReply")
                                        },
                                        onMarkAsReadClick = { privateMessageView ->
                                            inboxViewModel.markPrivateMessageAsRead(
                                                privateMessageView = privateMessageView,
                                                account = acct,
                                                ctx = ctx,
                                            )
                                            homeViewModel.updateUnreads(privateMessageView)
                                        },
                                        onPersonClick = { personId ->
                                            personClickWrapper(
                                                personProfileViewModel,
                                                personId,
                                                account,
                                                navController,
                                                ctx
                                            )
                                        },
                                        account = acct,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
