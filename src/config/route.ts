
import Index from "../pages/Index.vue";
import Team from "../pages/TeamPage.vue";
import UserPage from "../pages/UserPage.vue";
import UserUpdatePage from "../pages/UserUpdatePage.vue";
import UserTeamJoinPage from "../pages/UserTeamJoinPage.vue";
import UserTeamCreatePage from "../pages/UserTeamCreatePage.vue";
import MatchPage from "../pages/MatchPage.vue";
import MatchResultPage from "../pages/MatchResultPage.vue";
import UserEditPage from "../pages/UserEditPage.vue";
import UserLoginPage from "../pages/UserLoginPage.vue";
import TeamAddPage from "../pages/TeamAddPage.vue";
import TeamUpdatePage from "../pages/TeamUpdatePage.vue";
import UserRegistPage from "../pages/UserRegistPage.vue";
import PostDetail from "../pages/PostDetail.vue";
import Search from "../pages/Search.vue"
import WriteArticle from "../pages/WriteArticle.vue";
import CaloriesPage from "../pages/CaloriesPage.vue";
import PhotoAnalyse from "../pages/PhotoAnalyse.vue";
import UserDetailPage from "../pages/UserDetailPage.vue";
import CaloriesAnalysisPage from "../../forum-frontend-master/src/pages/CaloriesAnalysisPage.vue";
import MyPostFavour from "../pages/MyPostFavour.vue";
import FollowList from "../pages/FollowList.vue";


const routes = [
    {path: '/', component: Index},
    {path: '/search', title: '搜索', component: Search},
    {path: '/team', title: '找队伍', component: Team},
    {path: '/team/add', title: '创建队伍', component: TeamAddPage},
    {path: '/team/update', title: '更新队伍', component: TeamUpdatePage},
    {path: '/user', title: '个人信息', component: UserPage},
    {path: '/match', title: '匹配', component: MatchPage},
    {path: '/matchResult/list', title: '匹配历史记录', component: MatchResultPage},
    {path: '/user/edit', title: '编辑信息', component: UserEditPage},
    {path: '/user/login', title: '登录', component: UserLoginPage},
    {path: '/user/update', title: '更新信息', component: UserUpdatePage},
    {path: '/user/team/join', title: '加入队伍', component: UserTeamJoinPage},
    {path: '/user/team/create', title: '创建队伍', component: UserTeamCreatePage},
    {path: '/user/regist', title: '注册', component: UserRegistPage},
    {path: '/post/postDetail', name: 'PostDetail', title:'帖子详情页', component: PostDetail},
    {path: '/post/postDetail', name: 'PostDetail', title:'帖子详情页', component: PostDetail},
    {path: '/post/write-article', name: 'WriteArticle', title:'输入文章页', component: WriteArticle},
    {path: '/calories-page', name: 'CaloriesPage', title:'卡路里追踪', component: CaloriesPage},
    {path: '/photoAnalyse', name: 'PhotoAnalyse', title:'卡路里分析', component: PhotoAnalyse},
    {path: '/userDetailPage', name: 'UserDetailPage', title:'用户详情页', component: UserDetailPage},
    {path: '/caloriesAnalysis', name: 'CaloriesAnalysisPage', title:'用户详情页', component: CaloriesAnalysisPage},
    {path: '/myPostFavour', name: 'MyPostFavour', title:'帖子收藏', component: MyPostFavour},
    {path: '/followList', name: 'FollowList', title:'关注列表', component: FollowList},

]

export default routes;
