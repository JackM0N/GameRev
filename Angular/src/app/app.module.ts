import { inject, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button'; 
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { JwtModule } from '@auth0/angular-jwt';
import { AuthService } from './services/auth.service';
import { MatDialogModule } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToasterModule, ToasterService } from 'angular-toaster';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { GameService } from './services/game.service';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { TagService } from './services/tag.service';
import { DatePipe } from '@angular/common';
import { UserReviewService } from './services/user-review.service';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSortModule } from '@angular/material/sort';
import { ReportService } from './services/report.service';
import { MatExpansionModule } from '@angular/material/expansion';
import { LibraryService } from './services/library.service';
import { ImageCacheService } from './services/imageCache.service';
import { AdService } from './services/ad.service';
import { BackgroundService } from './services/background.service';
import { QuillModule } from 'ngx-quill';
import { AdBoxComponent } from './components/general-components/ad-box.component';
import { PopupDialogComponent } from './components/general-components/popup-dialog.component';
import { LoginComponent } from './components/page-components/authentication/login.component';
import { RegistrationComponent } from './components/page-components/authentication/registration.component';
import { ResetPasswordConfirmationDialogComponent } from './components/page-components/authentication/reset-password-confirmation-dialog.component';
import { CriticReviewListComponent } from './components/page-components/critic-reviews/critic-review-list.component';
import { GameInformationComponent } from './components/page-components/games/game-information/game-information.component';
import { ReviewReportDialogComponent } from './components/page-components/games/review-report-dialog.component';
import { LibraryComponent } from './components/page-components/library/library.component';
import { UserReviewListComponent } from './components/page-components/user-reviews/user-review-list.component';
import { AccountDeletionConfirmationDialogComponent } from './components/page-components/user/account-deletion-confirmation-dialog.component';
import { OwnProfileComponent } from './components/page-components/user/own-profile/own-profile.component';
import { ProfileComponent } from './components/page-components/user/profile.component';
import { UserListComponent } from './components/page-components/user/user-list.component';
import { GameListComponent } from './components/page-components/games/game-list.component';
import { AdminReportListComponent } from './components/page-components/reports/admin-report-list.component';
import { GameInfoCriticReviewComponent } from './components/page-components/games/game-information/critic-review.component';
import { GameInfoReviewListComponent } from './components/page-components/games/game-information/review-list.component';
import { NotificationService } from './services/notification.service';
import { EmailChangeComponent } from './components/page-components/user/own-profile/email-change.component';
import { PasswordChangeComponent } from './components/page-components/user/own-profile/password-change-component';
import { MatSliderModule } from '@angular/material/slider';
import { ForumComponent } from './components/page-components/forum/forum.component';
import { ForumService } from './services/forum.service';
import { ForumPostService } from './services/forumPost.service';
import { ForumPostListComponent } from './components/page-components/forum/forum-post-list.component';
import { ForumPostComponent } from './components/page-components/forum/forum-post.component';
import { ForumCommentEditDialogComponent } from './components/page-components/forum/forum-comment-edit-dialog.component';
import { ForumFormDialogComponent } from './components/page-components/forum/forum-form-dialog.component';
import { ForumPostFormDialogComponent } from './components/page-components/forum/forum-post-form-dialog.component';
import { GameFormDialogComponent } from './components/page-components/games/game-form-dialog.component';
import { LibraryFormDialogComponent } from './components/page-components/library/library-form-dialog.component';
import { ForumRequestsComponent } from './components/page-components/forum/forum-requests.component';
import { UserReviewFormDialogComponent } from './components/page-components/user-reviews/user-review-form-dialog.component';
import { CriticReviewFormDialogComponent } from './components/page-components/critic-reviews/critic-review-form-dialog.component';
import { ForumCommentFormComponent } from './components/page-components/forum/forum-comment-form.component';
import { CriticReviewContentDialogComponent } from './components/page-components/critic-reviews/critic-review-content-dialog.component';
import { UserReportListComponent } from './components/page-components/reports/user-report-list.component';
import { NotFoundComponent } from './components/page-components/not-found/not-found.component';

@NgModule({
    declarations: [
      AppComponent,
      LoginComponent,
      RegistrationComponent,
      OwnProfileComponent,
      AccountDeletionConfirmationDialogComponent,
      GameListComponent,
      GameInformationComponent,
      UserListComponent,
      PopupDialogComponent,
      ProfileComponent,
      ReviewReportDialogComponent,
      AdminReportListComponent,
      LibraryComponent,
      LibraryFormDialogComponent,
      UserReviewListComponent,
      ResetPasswordConfirmationDialogComponent,
      AdBoxComponent,
      CriticReviewListComponent,
      GameInfoCriticReviewComponent,
      GameInfoReviewListComponent,
      EmailChangeComponent,
      PasswordChangeComponent,
      ForumComponent,
      ForumPostListComponent,
      ForumPostComponent,
      ForumCommentEditDialogComponent,
      ForumFormDialogComponent,
      ForumPostFormDialogComponent,
      GameFormDialogComponent,
      ForumRequestsComponent,
      UserReviewFormDialogComponent,
      CriticReviewFormDialogComponent,
      ForumCommentFormComponent,
      CriticReviewContentDialogComponent,
      UserReportListComponent,
      NotFoundComponent,
    ],
    bootstrap: [
      AppComponent
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
      MatDatepickerModule,
      MatNativeDateModule,
      MatSelectModule,
      MatTableModule,
      MatPaginatorModule,
      MatButtonToggleModule,
      MatSortModule,
      MatExpansionModule,
      MatSliderModule,
      QuillModule.forRoot(),
      JwtModule.forRoot({
        config: {
          tokenGetter: () => {
            const authService = inject(AuthService);
            return authService.getToken();
          },
          allowedDomains: [''],
          disallowedRoutes: ['localhost:8080/login']
        }
      }),
      BrowserAnimationsModule,
      ToasterModule.forRoot()
    ],
    providers: [
      provideAnimationsAsync(),
      AuthService,
      ToasterService,
      GameService,
      TagService,
      UserReviewService,
      ReportService,
      LibraryService,
      ImageCacheService,
      BackgroundService,
      AdService,
      NotificationService,
      ForumService,
      ForumPostService,
      { provide: MAT_DATE_LOCALE, useValue: 'en-US' },
      DatePipe,
      provideHttpClient(withInterceptorsFromDi())
    ]
})
export class AppModule {}
