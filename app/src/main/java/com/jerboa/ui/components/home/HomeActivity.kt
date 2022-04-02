package com.jerboa.ui.components.home

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jerboa.VoteType
import com.jerboa.closeDrawer
import com.jerboa.db.Account
import com.jerboa.db.AccountViewModel
import com.jerboa.fetchInitialData
import com.jerboa.openLink
import com.jerboa.scrollToTop
import com.jerboa.ui.components.common.BottomAppBarAll
import com.jerboa.ui.components.common.WindowSize
import com.jerboa.ui.components.common.getCurrentAccount
import com.jerboa.ui.components.community.CommunityViewModel
import com.jerboa.ui.components.community.communityClickWrapper
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
import kotlinx.coroutines.CoroutineScope

@Composable
fun HomeActivity(
    navController: NavController,
    windowSize: WindowSize,
    homeViewModel: HomeViewModel,
    communityViewModel: CommunityViewModel,
    personProfileViewModel: PersonProfileViewModel,
    postViewModel: PostViewModel,
    inboxViewModel: InboxViewModel,
    accountViewModel: AccountViewModel,
    siteViewModel: SiteViewModel,
    postEditViewModel: PostEditViewModel,
    createReportViewModel: CreateReportViewModel,
) {

    Log.d("jerboa", "got to home activity")

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
                MainTopBar(
                    scope = scope,
                    postListState = postListState,
                    scaffoldState = scaffoldState,
                    homeViewModel = homeViewModel,
                    account = account,
                    ctx = ctx,
                    navController = navController,
                )
            },
            drawerShape = MaterialTheme.shapes.small,
            drawerElevation = 2.dp,
            drawerContent = {
                MainDrawer(
                    siteViewModel = siteViewModel,
                    accounts = accounts,
                    navController = navController,
                    accountViewModel = accountViewModel,
                    communityViewModel = communityViewModel,
                    homeViewModel = homeViewModel,
                    personProfileViewModel = personProfileViewModel,
                    inboxViewModel = inboxViewModel,
                    scope = scope,
                    scaffoldState = scaffoldState,
                    account = account,
                    ctx = ctx
                )
            },
            content = {
                MainPostListingsContent(
                    padding = it,
                    windowSize = windowSize,
                    homeViewModel = homeViewModel,
                    communityViewModel = communityViewModel,
                    personProfileViewModel = personProfileViewModel,
                    postViewModel = postViewModel,
                    postEditViewModel = postEditViewModel,
                    createReportViewModel = createReportViewModel,
                    account = account,
                    ctx = ctx,
                    navController = navController,
                    postListState = postListState,
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        account?.also {
                            navController.navigate("createPost")
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "TODO")
                }
            },
            bottomBar = {
                BottomAppBarAll(
                    screen = "home",
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

@Composable
fun MainPostListingsContent(
    homeViewModel: HomeViewModel,
    windowSize: WindowSize,
    communityViewModel: CommunityViewModel,
    personProfileViewModel: PersonProfileViewModel,
    postViewModel: PostViewModel,
    postEditViewModel: PostEditViewModel,
    createReportViewModel: CreateReportViewModel,
    account: Account?,
    ctx: Context,
    navController: NavController,
    padding: PaddingValues,
    postListState: LazyListState,
) {
    PostListings(
        listState = postListState,
        windowSize = windowSize,
        padding = padding,
        posts = homeViewModel.posts,
        onUpvoteClick = { postView ->
            homeViewModel.likePost(
                voteType = VoteType.Upvote,
                postView = postView,
                account = account,
                ctx = ctx,
            )
        },
        onDownvoteClick = { postView ->
            homeViewModel.likePost(
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
            homeViewModel.savePost(
                postView = postView,
                account = account,
                ctx = ctx,
            )
        },
        onBlockCommunityClick = {
            account?.also { acct ->
                homeViewModel.blockCommunity(
                    community = it,
                    account = acct,
                    ctx = ctx,
                )
            }
        },
        onBlockCreatorClick = {
            account?.also { acct ->
                homeViewModel.blockCreator(
                    creator = it,
                    account = acct,
                    ctx = ctx,
                )
            }
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
        onCommunityClick = { community ->
            communityClickWrapper(
                communityViewModel = communityViewModel,
                communityId = community.id,
                account = account,
                navController = navController,
                ctx = ctx,
            )
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
        onSwipeRefresh = {
            homeViewModel.fetchPosts(
                account = account,
                clear = true,
                ctx = ctx,
            )
        },
        loading = homeViewModel.loading.value &&
            homeViewModel.page.value == 1 &&
            homeViewModel.posts.isNotEmpty(),
        isScrolledToEnd = {
            if (homeViewModel.posts.size > 0) {
                homeViewModel.fetchPosts(
                    account = account,
                    nextPage = true,
                    ctx = ctx,
                )
            }
        },
        account = account,
    )
}

@Composable
fun MainDrawer(
    siteViewModel: SiteViewModel,
    accounts: List<Account>?,
    navController: NavController,
    accountViewModel: AccountViewModel,
    communityViewModel: CommunityViewModel,
    personProfileViewModel: PersonProfileViewModel,
    inboxViewModel: InboxViewModel,
    homeViewModel: HomeViewModel,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    account: Account?,
    ctx: Context
) {
    Drawer(
        myUserInfo = siteViewModel.siteRes?.my_user,
        unreadCounts = homeViewModel.unreadCountResponse,
        accounts = accounts,
        navController = navController,
        onSwitchAccountClick = { acct ->
            accountViewModel.removeCurrent()
            accountViewModel.setCurrent(acct.id)

            fetchInitialData(
                account = acct,
                siteViewModel = siteViewModel,
                homeViewModel = homeViewModel,
            )

            closeDrawer(scope, scaffoldState)
        },
        onSignOutClick = {
            accounts?.also { accounts ->
                getCurrentAccount(accounts)?.also {
                    accountViewModel.delete(it)
                    val updatedList = accounts.toMutableList()
                    updatedList.remove(it)

                    if (updatedList.isNotEmpty()) {
                        accountViewModel.setCurrent(updatedList[0].id)
                    }
                }
            }
        },
        onClickListingType = { listingType ->
            homeViewModel.fetchPosts(
                account = account,
                clear = true,
                changeListingType = listingType,
                ctx = ctx,
            )
            closeDrawer(scope, scaffoldState)
        },
        onCommunityClick = { community ->
            communityClickWrapper(
                communityViewModel,
                community.id,
                account,
                navController,
                ctx = ctx,
            )
            closeDrawer(scope, scaffoldState)
        },
        onClickProfile = {
            account?.id?.also {
                personClickWrapper(
                    personProfileViewModel = personProfileViewModel,
                    personId = it,
                    account = account,
                    navController = navController,
                    ctx = ctx,
                    saved = false,
                )
                closeDrawer(scope, scaffoldState)
            }
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
                closeDrawer(scope, scaffoldState)
            }
        },
        onClickInbox = {
            inboxClickWrapper(
                inboxViewModel = inboxViewModel,
                account = account,
                navController = navController,
                ctx = ctx,
            )
            closeDrawer(scope, scaffoldState)
        },
    )
}

@Composable
fun MainTopBar(
    scope: CoroutineScope,
    postListState: LazyListState,
    scaffoldState: ScaffoldState,
    homeViewModel: HomeViewModel,
    account: Account?,
    ctx: Context,
    navController: NavController,
) {

    Column {
        HomeHeader(
            scope = scope,
            scaffoldState = scaffoldState,
            navController = navController,
            selectedSortType = homeViewModel.sortType.value,
            selectedListingType = homeViewModel.listingType.value,
            onClickSortType = { sortType ->
                scrollToTop(scope, postListState)
                homeViewModel.fetchPosts(
                    account = account,
                    clear = true,
                    changeSortType = sortType,
                    ctx = ctx,
                )
            },
            onClickListingType = { listingType ->
                scrollToTop(scope, postListState)
                homeViewModel.fetchPosts(
                    account = account,
                    clear = true,
                    changeListingType = listingType,
                    ctx = ctx,
                )
            },
            onClickRefresh = {
                scrollToTop(scope, postListState)
                homeViewModel.fetchPosts(
                    account = account,
                    clear = true,
                    ctx = ctx,
                )
            }
        )
        if (homeViewModel.loading.value) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}
