import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegistrationComponent } from './components/authentication/registration/registration.component';
import { LoginComponent } from './components/authentication/login/login.component';
import { ProfileComponent } from './components/user/profile/profile.component';
import { GamesListComponent } from './components/games/games-list/games-list.component';
import { GameFormComponent } from './components/games/game-form/game-form.component';
import { GameInformationComponent } from './components/games/game-information/game-information.component';
import { UserReviewFormComponent } from './components/user-reviews/user-review-form/user-review-form.component';
import { UsersListComponent } from './components/user/users-list/users-list.component';

const routes: Routes = [
  { path: 'register', component: RegistrationComponent},
  { path: 'login', component: LoginComponent},
  { path: 'profile', component: ProfileComponent},
  { path: 'game/:name', component: GameInformationComponent},
  { path: 'games', component: GamesListComponent},
  { path: 'games/add', component: GameFormComponent},
  { path: 'games/edit/:name', component: GameFormComponent},
  { path: 'user-reviews/add/:name', component: UserReviewFormComponent},
  { path: 'user-reviews/edit/:id', component: UserReviewFormComponent},
  { path: 'users', component: UsersListComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
