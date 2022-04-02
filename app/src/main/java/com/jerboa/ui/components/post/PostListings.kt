package com.jerboa.ui.components.post

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.jerboa.datatypes.CommunitySafe
import com.jerboa.datatypes.PersonSafe
import com.jerboa.datatypes.PostView
import com.jerboa.datatypes.samplePostView
import com.jerboa.db.Account
import com.jerboa.isScrolledToEnd
import com.jerboa.ui.components.common.WindowSize

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostListings(
    windowSize: WindowSize,
    posts: List<PostView>,
    contentAboveListings: @Composable () -> Unit = {},
    onUpvoteClick: (postView: PostView) -> Unit,
    onDownvoteClick: (postView: PostView) -> Unit,
    onPostClick: (postView: PostView) -> Unit,
    onPostLinkClick: (url: String) -> Unit,
    onSaveClick: (postView: PostView) -> Unit,
    onEditPostClick: (postView: PostView) -> Unit,
    onReportClick: (postView: PostView) -> Unit,
    onCommunityClick: (community: CommunitySafe) -> Unit,
    onPersonClick: (personId: Int) -> Unit,
    onBlockCommunityClick: (community: CommunitySafe) -> Unit,
    onBlockCreatorClick: (person: PersonSafe) -> Unit,
    onSwipeRefresh: () -> Unit,
    loading: Boolean = false,
    isScrolledToEnd: () -> Unit,
    account: Account?,
    showCommunityName: Boolean = true,
    padding: PaddingValues = PaddingValues(0.dp),
    listState: LazyListState,
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(loading),
        onRefresh = onSwipeRefresh,
    ) {

        if (windowSize == WindowSize.Expanded) {
            LazyVerticalGrid(
                cells = GridCells.Adaptive(minSize = 400.dp)
                // listState = listState,
            ) {
                // List of items
                items(posts) { postView ->
                    PostListing(
                        postView = postView,
                        onUpvoteClick = onUpvoteClick,
                        onDownvoteClick = onDownvoteClick,
                        onPostClick = onPostClick,
                        onPostLinkClick = onPostLinkClick,
                        onSaveClick = onSaveClick,
                        onCommunityClick = onCommunityClick,
                        onPersonClick = onPersonClick,
                        onEditPostClick = onEditPostClick,
                        onReportClick = onReportClick,
                        onBlockCommunityClick = onBlockCommunityClick,
                        onBlockCreatorClick = onBlockCreatorClick,
                        account = account,
                        showCommunityName = showCommunityName,
                        isModerator = false // TODO can't know with many posts
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(padding)
                // .simpleVerticalScrollbar(listState)
            ) {
                // TODO this should be a .also?
                item {
                    contentAboveListings()
                }

                // List of items
                items(posts) { postView ->
                    PostListing(
                        postView = postView,
                        onUpvoteClick = onUpvoteClick,
                        onDownvoteClick = onDownvoteClick,
                        onPostClick = onPostClick,
                        onPostLinkClick = onPostLinkClick,
                        onSaveClick = onSaveClick,
                        onCommunityClick = onCommunityClick,
                        onPersonClick = onPersonClick,
                        onEditPostClick = onEditPostClick,
                        onReportClick = onReportClick,
                        onBlockCommunityClick = onBlockCommunityClick,
                        onBlockCreatorClick = onBlockCreatorClick,
                        account = account,
                        showCommunityName = showCommunityName,
                        isModerator = false // TODO can't know with many posts
                    )
                }
            }
        }
    }
    // observer when reached end of list
    val endOfListReached by remember {
        derivedStateOf {
            listState.isScrolledToEnd()
        }
    }

    // act when end of list reached
    if (endOfListReached) {
        LaunchedEffect(endOfListReached) {
            isScrolledToEnd()
        }
    }
}

@Preview
@Composable
fun PreviewPostListings() {
    PostListings(
        posts = listOf(samplePostView, samplePostView),
        account = null,
        onReportClick = {},
        isScrolledToEnd = {},
        onCommunityClick = {},
        onEditPostClick = {},
        onDownvoteClick = {},
        onUpvoteClick = {},
        onPersonClick = {},
        onSaveClick = {},
        onPostLinkClick = {},
        onPostClick = {},
        onSwipeRefresh = {},
        onBlockCreatorClick = {},
        onBlockCommunityClick = {},
        listState = rememberLazyListState(),
        windowSize = WindowSize.Compact,
    )
}
