import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/page-components/authentication/login.component';
import { RegistrationComponent } from './components/page-components/authentication/registration.component';
import { CriticReviewListComponent } from './components/page-components/critic-reviews/critic-review-list.component';
import { GameInformationComponent } from './components/page-components/games/game-information/game-information.component';
import { GameListComponent } from './components/page-components/games/game-list.component';
import { AdminReportListComponent } from './components/page-components/reports/admin-report-list.component';
import { UserReviewListComponent } from './components/page-components/user-reviews/user-review-list.component';
import { OwnProfileComponent } from './components/page-components/user/own-profile/own-profile.component';
import { ProfileComponent } from './components/page-components/user/profile.component';
import { LibraryComponent } from './components/page-components/library/library.component';
import { UserListComponent } from './components/page-components/user/user-list.component';
import { authGuard } from './auth.guard';
import { ForumComponent } from './components/page-components/forum/forum.component';
import { ForumPostComponent } from './components/page-components/forum/forum-post.component';
import { ForumRequestsComponent } from './components/page-components/forum/forum-requests.component';
import { UserReportListComponent } from './components/page-components/reports/user-report-list.component';
import { NotFoundComponent } from './components/page-components/not-found/not-found.component';

const routes: Routes = [
  { path: '', component: ForumComponent},
  { path: 'forum/:id', component: ForumComponent},
  { path: 'forum/:forumid/post/:postid', component: ForumPostComponent},
  { path: 'forum-requests', component: ForumRequestsComponent},
  { path: 'register', component: RegistrationComponent},
  { path: 'login', component: LoginComponent},
  { path: 'profile', component: OwnProfileComponent, canActivate: [authGuard]},
  { path: 'profile/:name', component: ProfileComponent},
  { path: 'game/:name', component: GameInformationComponent},
  { path: 'games', component: GameListComponent},
  { path: 'user-reviews', component: UserReviewListComponent, canActivate: [authGuard]},
  { path: 'user-reviews/:name', component: UserReviewListComponent, canActivate: [authGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'user-reports', component: UserReportListComponent, canActivate: [authGuard]},
  { path: 'users', component: UserListComponent, canActivate: [authGuard]},
  { path: 'reports', component: AdminReportListComponent, canActivate: [authGuard], data: { roles: ['Admin'] }},
  { path: 'library', component: LibraryComponent, canActivate: [authGuard]},
  { path: 'critic-reviews', component: CriticReviewListComponent, canActivate: [authGuard], data: { roles: ['Admin', 'Critic'] }},

  { path: '**', component: NotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
