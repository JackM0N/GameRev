import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegistrationComponent } from './components/authentication/registration/registration.component';
import { LoginComponent } from './components/authentication/login/login.component';
import { OwnProfileComponent } from './components/user/own-profile/own-profile.component';
import { GamesListComponent } from './components/games/games-list/games-list.component';
import { GameFormComponent } from './components/games/game-form/game-form.component';
import { GameInformationComponent } from './components/games/game-information/game-information.component';
import { UserReviewFormComponent } from './components/user-reviews/user-review-form/user-review-form.component';
import { UsersListComponent } from './components/user/users-list/users-list.component';
import { ProfileComponent } from './components/user/profile/profile.component';
import { ReportsListComponent } from './components/reports/reports-list/reports-list.component';
import { UserGamesListComponent } from './components/user-games/user-games-list/user-games-list.component';
import { UserReviewListComponent } from './components/reports/user-reviews-list/user-reviews-list.component';
import { CriticReviewFormComponent } from './components/critic-reviews/critic-review-form/critic-review-form.component';
import { CriticReviewListComponent } from './components/critic-reviews/critic-reviews-list/critic-reviews-list.component';

const routes: Routes = [
  { path: 'register', component: RegistrationComponent},
  { path: 'login', component: LoginComponent},
  { path: 'profile', component: OwnProfileComponent},
  { path: 'profile/:name', component: ProfileComponent},
  { path: 'game/:name', component: GameInformationComponent},
  { path: 'games', component: GamesListComponent},
  { path: 'games/add', component: GameFormComponent},
  { path: 'games/edit/:name', component: GameFormComponent},
  { path: 'user-reviews/add/:name', component: UserReviewFormComponent},
  { path: 'user-reviews/edit/:id', component: UserReviewFormComponent},
  { path: 'user-reviews', component: UserReviewListComponent},
  { path: 'user-reviews/:name', component: UserReviewListComponent},
  { path: 'users', component: UsersListComponent},
  { path: 'reports', component: ReportsListComponent},
  { path: 'library', component: UserGamesListComponent},
  { path: 'critic-reviews/add/:name', component: CriticReviewFormComponent},
  { path: 'critic-reviews/edit/:name', component: CriticReviewFormComponent},
  { path: 'critic-reviews', component: CriticReviewListComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
