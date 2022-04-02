package com.jerboa

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jerboa.db.AccountRepository
import com.jerboa.db.AccountViewModel
import com.jerboa.db.AccountViewModelFactory
import com.jerboa.db.AppDB
import com.jerboa.ui.components.comment.edit.CommentEditActivity
import com.jerboa.ui.components.comment.edit.CommentEditViewModel
import com.jerboa.ui.components.comment.reply.CommentReplyActivity
import com.jerboa.ui.components.comment.reply.CommentReplyViewModel
import com.jerboa.ui.components.common.getCurrentAccount
import com.jerboa.ui.components.common.rememberWindowSizeClass
import com.jerboa.ui.components.community.CommunityActivity
import com.jerboa.ui.components.community.CommunityViewModel
import com.jerboa.ui.components.community.list.CommunityListActivity
import com.jerboa.ui.components.community.list.CommunityListViewModel
import com.jerboa.ui.components.community.sidebar.CommunitySidebarActivity
import com.jerboa.ui.components.home.*
import com.jerboa.ui.components.inbox.InboxActivity
import com.jerboa.ui.components.inbox.InboxViewModel
import com.jerboa.ui.components.login.LoginActivity
import com.jerboa.ui.components.login.LoginViewModel
import com.jerboa.ui.components.person.PersonProfileActivity
import com.jerboa.ui.components.person.PersonProfileViewModel
import com.jerboa.ui.components.post.PostActivity
import com.jerboa.ui.components.post.PostViewModel
import com.jerboa.ui.components.post.create.CreatePostActivity
import com.jerboa.ui.components.post.create.CreatePostViewModel
import com.jerboa.ui.components.post.edit.PostEditActivity
import com.jerboa.ui.components.post.edit.PostEditViewModel
import com.jerboa.ui.components.private_message.PrivateMessageReplyActivity
import com.jerboa.ui.components.report.CreateReportViewModel
import com.jerboa.ui.components.report.comment.CreateCommentReportActivity
import com.jerboa.ui.components.report.post.CreatePostReportActivity
import com.jerboa.ui.theme.JerboaTheme

class JerboaApplication : Application() {
    private val database by lazy { AppDB.getDatabase(this) }
    val repository by lazy { AccountRepository(database.accountDao()) }
}

class MainActivity : ComponentActivity() {

    private val homeViewModel by viewModels<HomeViewModel>()
    private val postViewModel by viewModels<PostViewModel>()
    private val loginViewModel by viewModels<LoginViewModel>()
    private val siteViewModel by viewModels<SiteViewModel>()
    private val communityViewModel by viewModels<CommunityViewModel>()
    private val personProfileViewModel by viewModels<PersonProfileViewModel>()
    private val inboxViewModel by viewModels<InboxViewModel>()
    private val communityListViewModel by viewModels<CommunityListViewModel>()
    private val createPostViewModel by viewModels<CreatePostViewModel>()
    private val commentReplyViewModel by viewModels<CommentReplyViewModel>()
    private val commentEditViewModel by viewModels<CommentEditViewModel>()
    private val postEditViewModel by viewModels<PostEditViewModel>()
    private val createReportViewModel by viewModels<CreateReportViewModel>()

    private val accountViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory((application as JerboaApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val account = getCurrentAccount(accountViewModel.allAccountSync)
        fetchInitialData(account, siteViewModel, homeViewModel)

        setContent {
            JerboaTheme {
                val windowSize = rememberWindowSizeClass()

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splashScreen",
                ) {
                    composable(route = "login") {
                        LoginActivity(
                            navController = navController,
                            loginViewModel = loginViewModel,
                            accountViewModel = accountViewModel,
                            siteViewModel = siteViewModel,
                            homeViewModel = homeViewModel,
                        )
                    }
                    composable(route = "splashScreen") {
                        SplashScreenActivity(
                            navController = navController,
                        )
                    }
                    composable(route = "home") {
                        HomeActivity(
                            navController = navController,
                            windowSize = windowSize,
                            homeViewModel = homeViewModel,
                            communityViewModel = communityViewModel,
                            personProfileViewModel = personProfileViewModel,
                            postViewModel = postViewModel,
                            inboxViewModel = inboxViewModel,
                            accountViewModel = accountViewModel,
                            siteViewModel = siteViewModel,
                            postEditViewModel = postEditViewModel,
                            createReportViewModel = createReportViewModel,
                        )
                    }
                    composable(route = "community") {
                        CommunityActivity(
                            navController = navController,
                            windowSize = windowSize,
                            communityViewModel = communityViewModel,
                            personProfileViewModel = personProfileViewModel,
                            postViewModel = postViewModel,
                            accountViewModel = accountViewModel,
                            homeViewModel = homeViewModel,
                            inboxViewModel = inboxViewModel,
                            postEditViewModel = postEditViewModel,
                            createReportViewModel = createReportViewModel,
                        )
                    }
                    composable(
                        route = "profile?saved={saved}",
                        arguments = listOf(
                            navArgument("saved") {
                                defaultValue = false
                                type = NavType.BoolType
                            }
                        )
                    ) {
                        PersonProfileActivity(
                            savedMode = it.arguments?.getBoolean("saved")!!,
                            navController = navController,
                            windowSize = windowSize,
                            personProfileViewModel = personProfileViewModel,
                            postViewModel = postViewModel,
                            communityViewModel = communityViewModel,
                            accountViewModel = accountViewModel,
                            homeViewModel = homeViewModel,
                            inboxViewModel = inboxViewModel,
                            commentEditViewModel = commentEditViewModel,
                            commentReplyViewModel = commentReplyViewModel,
                            postEditViewModel = postEditViewModel,
                            createReportViewModel = createReportViewModel,
                        )
                    }
                    composable(
                        route = "communityList?select={select}",
                        arguments = listOf(
                            navArgument("select") {
                                defaultValue = false
                                type = NavType.BoolType
                            }
                        )
                    ) {
                        // Whenever navigating here, reset the list with your followed communities
                        communityListViewModel.setCommunityListFromFollowed(siteViewModel)

                        CommunityListActivity(
                            navController = navController,
                            accountViewModel = accountViewModel,
                            communityViewModel = communityViewModel,
                            communityListViewModel = communityListViewModel,
                            selectMode = it.arguments?.getBoolean("select")!!
                        )
                    }
                    composable(route = "createPost") {
                        CreatePostActivity(
                            navController = navController,
                            accountViewModel = accountViewModel,
                            createPostViewModel = createPostViewModel,
                            communityListViewModel = communityListViewModel,
                            postViewModel = postViewModel,
                        )
                    }
                    composable(route = "inbox") {
                        InboxActivity(
                            navController = navController,
                            inboxViewModel = inboxViewModel,
                            personProfileViewModel = personProfileViewModel,
                            postViewModel = postViewModel,
                            communityViewModel = communityViewModel,
                            accountViewModel = accountViewModel,
                            homeViewModel = homeViewModel,
                            commentEditViewModel = commentEditViewModel,
                            commentReplyViewModel = commentReplyViewModel,
                            createReportViewModel = createReportViewModel,
                        )
                    }
                    composable(
                        route = "post",
                    ) {
                        PostActivity(
                            postViewModel = postViewModel,
                            accountViewModel = accountViewModel,
                            communityViewModel = communityViewModel,
                            personProfileViewModel = personProfileViewModel,
                            commentEditViewModel = commentEditViewModel,
                            commentReplyViewModel = commentReplyViewModel,
                            postEditViewModel = postEditViewModel,
                            createReportViewModel = createReportViewModel,
                            navController = navController,
                        )
                    }
                    composable(
                        route = "commentReply",
                    ) {
                        CommentReplyActivity(
                            commentReplyViewModel = commentReplyViewModel,
                            postViewModel = postViewModel,
                            accountViewModel = accountViewModel,
                            personProfileViewModel = personProfileViewModel,
                            inboxViewModel = inboxViewModel,
                            navController = navController,
                        )
                    }
                    composable(
                        route = "siteSidebar",
                    ) {
                        SiteSidebarActivity(
                            siteViewModel = siteViewModel,
                            navController = navController,
                        )
                    }
                    composable(
                        route = "communitySidebar",
                    ) {
                        CommunitySidebarActivity(
                            communityViewModel = communityViewModel,
                            navController = navController,
                        )
                    }
                    composable(
                        route = "commentEdit",
                    ) {
                        CommentEditActivity(
                            commentEditViewModel = commentEditViewModel,
                            accountViewModel = accountViewModel,
                            navController = navController,
                            personProfileViewModel = personProfileViewModel,
                            postViewModel = postViewModel,
                            inboxViewModel = inboxViewModel,
                        )
                    }
                    composable(
                        route = "postEdit",
                    ) {
                        PostEditActivity(
                            postEditViewModel = postEditViewModel,
                            communityViewModel = communityViewModel,
                            accountViewModel = accountViewModel,
                            navController = navController,
                            personProfileViewModel = personProfileViewModel,
                            postViewModel = postViewModel,
                            homeViewModel = homeViewModel,
                        )
                    }
                    composable(
                        route = "privateMessageReply",
                    ) {
                        PrivateMessageReplyActivity(
                            inboxViewModel = inboxViewModel,
                            accountViewModel = accountViewModel,
                            personProfileViewModel = personProfileViewModel,
                            navController = navController,
                        )
                    }
                    composable(
                        route = "commentReport",
                    ) {
                        CreateCommentReportActivity(
                            createReportViewModel = createReportViewModel,
                            accountViewModel = accountViewModel,
                            navController = navController,
                        )
                    }
                    composable(
                        route = "postReport",
                    ) {
                        CreatePostReportActivity(
                            createReportViewModel = createReportViewModel,
                            accountViewModel = accountViewModel,
                            navController = navController,
                        )
                    }
                }
            }
        }
    }
}
