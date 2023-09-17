package com.jerboa.model
import android.content.Context
import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jerboa.JerboaAppState
import com.jerboa.api.API
import com.jerboa.api.ApiState
import com.jerboa.api.apiWrapper
import com.jerboa.datatypes.types.BlockCommunity
import com.jerboa.datatypes.types.BlockCommunityResponse
import com.jerboa.datatypes.types.BlockPerson
import com.jerboa.datatypes.types.BlockPersonResponse
import com.jerboa.datatypes.types.CreatePostLike
import com.jerboa.datatypes.types.DeletePost
import com.jerboa.datatypes.types.GetPosts
import com.jerboa.datatypes.types.GetPostsResponse
import com.jerboa.datatypes.types.ListingType
import com.jerboa.datatypes.types.MarkPostAsRead
import com.jerboa.datatypes.types.PostId
import com.jerboa.datatypes.types.PostResponse
import com.jerboa.datatypes.types.PostView
import com.jerboa.datatypes.types.SavePost
import com.jerboa.datatypes.types.SortType
import com.jerboa.db.entity.Account
import com.jerboa.db.entity.AnonAccount
import com.jerboa.db.entity.getJWT
import com.jerboa.db.repository.AccountRepository
import com.jerboa.findAndUpdatePost
import com.jerboa.jerboaApplication
import com.jerboa.mergePosts
import com.jerboa.serializeToMap
import com.jerboa.showBlockCommunityToast
import com.jerboa.showBlockPersonToast
import com.jerboa.toEnumSafe
import com.jerboa.ui.components.common.PostStream
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HomeViewModel(private val accountRepository: AccountRepository) : ViewModel(), PostStream {
    var postsRes: ApiState<GetPostsResponse> by mutableStateOf(ApiState.Empty)
        private set

    private var likePostRes: ApiState<PostResponse> by mutableStateOf(ApiState.Empty)
    private var savePostRes: ApiState<PostResponse> by mutableStateOf(ApiState.Empty)
    private var deletePostRes: ApiState<PostResponse> by mutableStateOf(ApiState.Empty)
    private var blockCommunityRes: ApiState<BlockCommunityResponse> by mutableStateOf(ApiState.Empty)
    private var blockPersonRes: ApiState<BlockPersonResponse> by mutableStateOf(ApiState.Empty)

    val lazyListState = LazyListState()

    var sortType by mutableStateOf(SortType.Active)
        private set
    var listingType by mutableStateOf(ListingType.Local)
        private set
    var page by mutableIntStateOf(1)
        private set

    init {
        viewModelScope.launch {
            accountRepository.currentAccount
                .asFlow()
                .map { it ?: AnonAccount }
                .collect { account ->
                    updateSortType(account.defaultSortType.toEnumSafe())
                    updateListingType(account.defaultListingType.toEnumSafe())
                    Log.d("jerboa", "Fetching posts")
                    resetPosts(account)
                }
        }
    }

    fun updateSortType(sortType: SortType) {
        this.sortType = sortType
    }

    fun updateListingType(listingType: ListingType) {
        this.listingType = listingType
    }

    fun resetPage() {
        page = 1
    }

    fun nextPage() {
        page += 1
    }

    fun prevPage() {
        page -= 1
    }

    fun getPosts(form: GetPosts, state: ApiState<GetPostsResponse> = ApiState.Loading) {
        viewModelScope.launch {
            postsRes = state
            postsRes = apiWrapper(
                API.getInstance().getPosts(form.serializeToMap()),
            )
        }
    }

    fun appendPosts(jwt: String?) {
        viewModelScope.launch {
            fetchMore(jwt)
        }
    }
    private suspend fun fetchMore(jwt: String?) {
        val oldRes = postsRes
        postsRes = when (oldRes) {
            is ApiState.Appending -> return
            is ApiState.Holder -> ApiState.Appending(oldRes.data)
            else -> return
        }

        nextPage()
        val newRes = apiWrapper(API.getInstance().getPosts(getForm(jwt).serializeToMap()))

        postsRes = when (newRes) {
            is ApiState.Success -> {
                ApiState.Success(
                    GetPostsResponse(
                        mergePosts(
                            oldRes.data.posts,
                            newRes.data.posts,
                        ),
                    ),
                )
            }

            else -> {
                prevPage()
                ApiState.AppendingFailure(oldRes.data)
            }
        }
    }

    fun likePost(form: CreatePostLike) {
        viewModelScope.launch {
            likePostRes = ApiState.Loading
            likePostRes = apiWrapper(API.getInstance().likePost(form))

            when (val likeRes = likePostRes) {
                is ApiState.Success -> {
                    updatePost(likeRes.data.post_view)
                }

                else -> {}
            }
        }
    }

    fun savePost(form: SavePost) {
        viewModelScope.launch {
            savePostRes = ApiState.Loading
            savePostRes = apiWrapper(API.getInstance().savePost(form))
            when (val saveRes = savePostRes) {
                is ApiState.Success -> {
                    updatePost(saveRes.data.post_view)
                }

                else -> {}
            }
        }
    }

    fun deletePost(form: DeletePost) {
        viewModelScope.launch {
            deletePostRes = ApiState.Loading
            deletePostRes = apiWrapper(API.getInstance().deletePost(form))
            when (val deletePost = deletePostRes) {
                is ApiState.Success -> {
                    updatePost(deletePost.data.post_view)
                }

                else -> {}
            }
        }
    }

    fun blockCommunity(form: BlockCommunity, ctx: Context) {
        viewModelScope.launch {
            blockCommunityRes = ApiState.Loading
            blockCommunityRes =
                apiWrapper(API.getInstance().blockCommunity(form))
            showBlockCommunityToast(blockCommunityRes, ctx)
        }
    }

    fun blockPerson(form: BlockPerson, ctx: Context) {
        viewModelScope.launch {
            blockPersonRes = ApiState.Loading
            blockPersonRes = apiWrapper(API.getInstance().blockPerson(form))
            showBlockPersonToast(blockPersonRes, ctx)
        }
    }

    fun updatePost(postView: PostView) {
        when (val existing = postsRes) {
            is ApiState.Success -> {
                val newPosts = findAndUpdatePost(existing.data.posts, postView)
                val newRes = ApiState.Success(existing.data.copy(posts = newPosts))
                postsRes = newRes
            }

            else -> {}
        }
    }

    fun resetPosts(account: Account) {
        resetPage()
        getPosts(
            GetPosts(
                sort = sortType,
                type_ = listingType,
                auth = account.getJWT(),
            ),
        )
    }

    fun refreshPosts(account: Account) {
        resetPage()
        getPosts(
            GetPosts(
                page = page,
                sort = sortType,
                type_ = listingType,
                auth = account.getJWT(),
            ),
            ApiState.Refreshing,
        )
    }

    fun getForm(jwt: String?): GetPosts {
        return GetPosts(
            page = page,
            sort = sortType,
            type_ = listingType,
            auth = jwt,
        )
    }

    override fun getNextPost(current: PostId?, account: Account?): PostId? = runBlocking {
        val res = postsRes
        if (res is ApiState.Success) {
            if (current == null) {
                Log.i("HomeViewModel", "No current post selected")
                res.data.posts.firstOrNull()?.post?.id
            } else {
                res.data.posts
                    .mapIndexed { index, postView -> index to postView }
                    .firstOrNull { it.second.post.id == current }
                    ?.first?.let { currIndex ->
                        if (currIndex >= res.data.posts.size - 7) {
                            appendPosts(account?.jwt)
                            val nextIndex = currIndex + 1
                            if (res.data.posts.size > nextIndex) {
                                res.data.posts[nextIndex].post.id
                            } else {
                                null
                            }
                        } else if (currIndex >= 0) {
                            res.data.posts[currIndex + 1].post.id
                        } else {
                            res.data.posts.firstOrNull()?.post?.id
                        }
                    }
            }
        } else {
            null
        }
    }

    override fun getPreviousPost(current: PostId?, account: Account?): PostId? {
        val res = postsRes
        return if (res is ApiState.Success) {
            if (current == null) {
                Log.i("HomeViewModel", "No current post selected")
                res.data.posts.firstOrNull()?.post?.id
            } else {
                val currIndex = res.data.posts
                    .mapIndexed { index, postView -> index to postView }
                    .firstOrNull { it.second.post.id == current }
                    ?.first
                if (currIndex != null && currIndex > 0 && currIndex < res.data.posts.size) {
                    res.data.posts[currIndex - 1].post.id
                } else {
                    null
                }
            }
        } else {
            null
        }
    }

    override fun isFetchingMore(): Boolean = when (this.postsRes) {
        is ApiState.Loading -> true
        else -> false
    }

    fun markPostAsRead(
        form: MarkPostAsRead,
        appState: JerboaAppState,
    ) {
        appState.coroutineScope.launch {
            when (val markRes = apiWrapper(API.getInstance().markAsRead(form))) {
                is ApiState.Success -> {
                    updatePost(markRes.data.post_view)
                }

                else -> {}
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                HomeViewModel(jerboaApplication().container.accountRepository)
            }
        }
    }
}
