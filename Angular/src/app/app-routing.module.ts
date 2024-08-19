import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/page-components/authentication/login.component';
import { RegistrationComponent } from './components/page-components/authentication/registration.component';
import { CriticReviewFormComponent } from './components/page-components/critic-reviews/critic-review-form.component';
import { CriticReviewListComponent } from './components/page-components/critic-reviews/critic-review-list.component';
import { GameFormComponent } from './components/page-components/games/game-form.component';
import { GameInformationComponent } from './components/page-components/games/game-information/game-information.component';
import { GameListComponent } from './components/page-components/games/game-list.component';
import { ReportListComponent } from './components/page-components/reports/report-list.component';
import { UserReviewFormComponent } from './components/page-components/user-reviews/user-review-form.component';
import { UserReviewListComponent } from './components/page-components/user-reviews/user-review-list.component';
import { OwnProfileComponent } from './components/page-components/user/own-profile/own-profile.component';
import { ProfileComponent } from './components/page-components/user/profile.component';
import { LibraryComponent } from './components/page-components/library/library.component';
import { UserListComponent } from './components/page-components/user/user-list.component';
import { AuthGuard } from './auth.guard';

const routes: Routes = [
  { path: 'register', component: RegistrationComponent},
  { path: 'login', component: LoginComponent},
  { path: 'profile', component: OwnProfileComponent, canActivate: [AuthGuard]},
  { path: 'profile/:name', component: ProfileComponent},
  { path: 'game/:name', component: GameInformationComponent},
  { path: 'games', component: GameListComponent},
  { path: 'games/add', component: GameFormComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'games/edit/:name', component: GameFormComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'user-reviews/add/:name', component: UserReviewFormComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'user-reviews/edit/:id', component: UserReviewFormComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'user-reviews', component: UserReviewListComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'user-reviews/:name', component: UserReviewListComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'users', component: UserListComponent, canActivate: [AuthGuard]},
  { path: 'reports', component: ReportListComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'library', component: LibraryComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'critic-reviews/add/:name', component: CriticReviewFormComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'critic-reviews/edit/:id', component: CriticReviewFormComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
  { path: 'critic-reviews', component: CriticReviewListComponent, canActivate: [AuthGuard], data: { roles: ['Admin', 'Critic'] }},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
