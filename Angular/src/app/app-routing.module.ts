import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/page-components/authentication/login.component';
import { RegistrationComponent } from './components/page-components/authentication/registration.component';
import { CriticReviewListComponent } from './components/page-components/critic-reviews/critic-review-list.component';
import { GameInformationComponent } from './components/page-components/games/game-information/game-information.component';
import { GameListComponent } from './components/page-components/games/game-list.component';
import { ReportListComponent } from './components/page-components/reports/report-list.component';
import { UserReviewListComponent } from './components/page-components/user-reviews/user-review-list.component';
import { OwnProfileComponent } from './components/page-components/user/own-profile/own-profile.component';
import { ProfileComponent } from './components/page-components/user/profile.component';
import { LibraryComponent } from './components/page-components/library/library.component';
import { UserListComponent } from './components/page-components/user/user-list.component';
import { AuthGuard } from './auth.guard';
import { ForumComponent } from './components/page-components/forum/forum.component';
import { ForumPostComponent } from './components/page-components/forum/forum-post.component';
import { ForumRequestsComponent } from './components/page-components/forum/forum-requests.component';

const routes: Routes = [
  { path: '', component: ForumComponent},
  { path: 'forum/:id', component: ForumComponent},
  { path: 'forum/:forumid/post/:postid', component: ForumPostComponent},
  { path: 'forum-requests', component: ForumRequestsComponent},
  { path: 'register', component: RegistrationComponent},
  { path: 'login', component: LoginComponent},
  { path: 'profile', component: OwnProfileComponent, canActivate: [AuthGuard]},
  { path: 'profile/:name', component: ProfileComponent},
  { path: 'game/:name', component: GameInformationComponent},
  { path: 'games', component: GameListComponent},
  { path: 'user-reviews', component: UserReviewListComponent},
  { path: 'user-reviews/:name', component: UserReviewListComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'users', component: UserListComponent, canActivate: [AuthGuard]},
  { path: 'reports', component: ReportListComponent, canActivate: [AuthGuard], data: { roles: ['Admin'] }},
  { path: 'library', component: LibraryComponent, canActivate: [AuthGuard]},
  { path: 'critic-reviews', component: CriticReviewListComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
