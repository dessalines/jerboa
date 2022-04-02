package com.jerboa.ui.components.community

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.jerboa.VoteType
import com.jerboa.db.AccountViewModel
import com.jerboa.openLink
import com.jerboa.scrollToTop
import com.jerboa.ui.components.common.BottomAppBarAll
import com.jerboa.ui.components.common.WindowSize
import com.jerboa.ui.components.common.getCurrentAccount
import com.jerboa.ui.components.home.HomeViewModel
import com.jerboa.ui.components.inbox.InboxViewModel
import com.jerboa.ui.components.inbox.inboxClickWrapper
import com.jerboa.ui.components.person.PersonProfileViewModel
import com.jerboa.ui.components.person.personClickWrapper
import com.jerboa.ui.components.post.PostListings
import com.jerboa.ui.components.post.PostViewModel
import com.jerboa.ui.components.post.edit.PostEditViewModel
import com.jerboa.ui.components.post.edit.postEditClickWrapper
import com.jerboa.ui.components.post.postClickWrapper
import com.jerboa.ui.components.report.CreateReportViewModel
import com.jerboa.ui.components.report.postReportClickWrapper

@Composable
fun CommunityActivity(
    navController: NavController,
    windowSize: WindowSize,
    communityViewModel: CommunityViewModel,
    personProfileViewModel: PersonProfileViewModel,
    postViewModel: PostViewModel,
    accountViewModel: AccountViewModel,
    homeViewModel: HomeViewModel,
    inboxViewModel: InboxViewModel,
    postEditViewModel: PostEditViewModel,
    createReportViewModel: CreateReportViewModel,
) {

    Log.d("jerboa", "got to community activity")

    val scope = rememberCoroutineScope()
    val postListState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    val ctx = LocalContext.current
    val accounts by accountViewModel.allAccounts.observeAsState()
    val account = getCurrentAccount(accounts = accounts)

    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                Column {
                    communityViewModel.communityView?.community?.also { com ->
                        CommunityHeader(
                            communityName = com.name,
                            selectedSortType = communityViewModel.sortType.value,
                            onClickRefresh = {
                                scrollToTop(scope, postListState)
                                communityViewModel.fetchPosts(
                                    account = account,
                                    clear = true,
                                    ctx = ctx,
                                )
                            },
                            onClickSortType = { sortType ->
                                scrollToTop(scope, postListState)
                                communityViewModel.fetchPosts(
                                    account = account,
                                    clear = true,
                                    changeSortType = sortType,
                                    ctx = ctx,
                                )
                            },
                            onBlockCommunityClick = {
                                account?.also { acct ->
                                    communityViewModel.blockCommunity(
                                        account = acct,
                                        ctx = ctx,
                                    )
                                }
                            },
                            navController = navController,
                        )
                    }
                    if (communityViewModel.loading.value) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            content = {
                PostListings(
                    showCommunityName = false,
                    listState = postListState,
                    windowSize = windowSize,
                    padding = it,
                    contentAboveListings = {
                        communityViewModel.communityView?.also {
                            CommunityTopSection(
                                communityView = it,
                                onClickFollowCommunity = { cv ->
                                    communityViewModel.followCommunity(
                                        cv = cv,
                                        account = account,
                                        ctx = ctx,
                                    )
                                }
                            )
                        }
                    },
                    posts = communityViewModel.posts,
                    onUpvoteClick = { postView ->
                        communityViewModel.likePost(
                            voteType = VoteType.Upvote,
                            postView = postView,
                            account = account,
                            ctx = ctx,
                        )
                    },
                    onDownvoteClick = { postView ->
                        communityViewModel.likePost(
                            voteType = VoteType.Downvote,
                            postView = postView,
                            account = account,
                            ctx = ctx,
                        )
                    },
                    onPostClick = { postView ->
                        postClickWrapper(
                            postViewModel = postViewModel,
                            postId = postView.post.id,
                            account = account,
                            navController = navController,
                            ctx = ctx,
                        )
                    },
                    onPostLinkClick = { url ->
                        openLink(url, ctx)
                    },
                    onSaveClick = { postView ->
                        communityViewModel.savePost(
                            postView = postView,
                            account = account,
                            ctx = ctx,
                        )
                    },
                    onBlockCommunityClick = {
                        account?.also { acct ->
                            communityViewModel.blockCommunity(
                                account = acct,
                                ctx = ctx,
                            )
                        }
                    },
                    onBlockCreatorClick = {
                        account?.also { acct ->
                            communityViewModel.blockCreator(
                                creator = it,
                                account = acct,
                                ctx = ctx,
                            )
                        }
                    },
                    onCommunityClick = { community ->
                        communityClickWrapper(
                            communityViewModel,
                            community.id,
                            account,
                            navController,
                            ctx = ctx,
                        )
                    },
                    onEditPostClick = { postView ->
                        postEditClickWrapper(
                            postEditViewModel,
                            postView,
                            navController,
                        )
                    },
                    onReportClick = { postView ->
                        postReportClickWrapper(
                            createReportViewModel,
                            postView.post.id,
                            navController,
                        )
                    },
                    onSwipeRefresh = {
                        communityViewModel.fetchPosts(
                            account = account,
                            clear = true,
                            ctx = ctx,
                        )
                    },
                    loading = communityViewModel.loading.value &&
                        communityViewModel.page.value == 1 &&
                        communityViewModel.posts.isNotEmpty(),
                    isScrolledToEnd = {
                        if (communityViewModel.posts.size > 0) {
                            communityViewModel.fetchPosts(
                                account = account,
                                nextPage = true,
                                ctx = ctx,
                            )
                        }
                    },
                    onPersonClick = { personId ->
                        personClickWrapper(
                            personProfileViewModel = personProfileViewModel,
                            personId = personId,
                            account = account,
                            navController = navController,
                            ctx = ctx,
                        )
                    },
                    account = account,
                )
            },
            bottomBar = {
                BottomAppBarAll(
                    screen = "communityList",
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
