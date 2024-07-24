import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './components/authentication/login/login.component';
import { RegistrationComponent } from './components/authentication/registration/registration.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button'; 
import { HttpClientModule } from '@angular/common/http';
import { JwtModule } from '@auth0/angular-jwt';
import { AuthService } from './services/auth.service';
import { OwnProfileComponent } from './components/user/own-profile/own-profile.component';
import { MatDialogModule } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToasterModule, ToasterService } from 'angular-toaster';
import { AccountDeletionConfirmationDialogComponent } from './components/user/account-deletion-confirmation-dialog/account-deletion-confirmation-dialog.component';
import { GamesListComponent } from './components/games/games-list/games-list.component';
import { GameFormComponent } from './components/games/game-form/game-form.component';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { GameService } from './services/game.service';
import { ReleaseStatusService } from './services/release-status.service';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { TagService } from './services/tag.service';
import { DatePipe } from '@angular/common';
import { GameInformationComponent } from './components/games/game-information/game-information.component';
import { UserReviewService } from './services/user-review.service';
import { UserReviewFormComponent } from './components/user-reviews/user-review-form/user-review-form.component';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSortModule } from '@angular/material/sort';
import { UsersListComponent } from './components/user/users-list/users-list.component';
import { PopupDialogComponent } from './components/popup-dialog/popup-dialog.component';
import { ProfileComponent } from './components/user/profile/profile.component';
import { ReviewReportDialogComponent } from './components/games/review-report-dialog/review-report-dialog.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegistrationComponent,
    OwnProfileComponent,
    AccountDeletionConfirmationDialogComponent,
    GameFormComponent,
    GamesListComponent,
    GameInformationComponent,
    UserReviewFormComponent,
    UsersListComponent,
    PopupDialogComponent,
    ProfileComponent,
    ReviewReportDialogComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    HttpClientModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonToggleModule,
    MatSortModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: () => {
          return localStorage.getItem('access_token');
        },
        allowedDomains: [''],
        disallowedRoutes: ['localhost:8080/login']
      }
    }),
    BrowserAnimationsModule,
    ToasterModule.forRoot()
  ],
  providers: [
    provideClientHydration(),
    provideAnimationsAsync(),
    AuthService,
    ToasterService,
    GameService,
    ReleaseStatusService,
    TagService,
    UserReviewService,
    { provide: MAT_DATE_LOCALE, useValue: 'en-US' },
    DatePipe
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
