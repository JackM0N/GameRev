import { ChangeDetectorRef, Component, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { LoginCredentials } from '../../../models/loginCredentials';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { BackgroundService } from '../../../services/background.service';
import { ResetPasswordConfirmationDialogComponent } from './reset-password-confirmation-dialog.component';
import { NotificationService } from '../../../services/notification.service';
import { NotificationAction } from '../../../enums/notificationActions';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent extends BaseAdComponent implements OnInit {
  protected loginForm: FormGroup;
  protected hidePassword = signal(true);

  protected images = [
    // Left Side
    { classNames: 'smallBgImage1 animate-wiggle-slow absolute top-[12%] left-[15%]' },
    { classNames: 'smallBgImage2 animate-wiggle-reverse absolute top-[20%] left-[10%]' },
    { classNames: 'smallBgImage3 animate-wiggle absolute top-[35%] left-[25%]' },
    { classNames: 'smallBgImage4 animate-wiggle-slow absolute top-[50%] left-[12%]' },
    { classNames: 'smallBgImage5 animate-wiggle-reverse-slow absolute top-[60%] left-[8%]' },
    { classNames: 'smallBgImage6 animate-wiggle-reverse-slow absolute top-[75%] left-[18%]' },
    { classNames: 'smallBgImage7 animate-wiggle-slow absolute top-[90%] left-[20%]' },
    
    // Center
    { classNames: 'smallBgImage8 animate-wiggle-reverse absolute top-[10%] left-[45%]' },
    { classNames: 'smallBgImage9 animate-wiggle absolute top-[25%] left-[50%]' },
    { classNames: 'smallBgImage10 animate-wiggle-reverse-slow absolute top-[40%] left-[60%]' },
    { classNames: 'smallBgImage11 animate-wiggle-reverse absolute top-[55%] left-[55%]' },
    { classNames: 'smallBgImage12 animate-wiggle-slow absolute top-[70%] left-[40%]' },
    
    // Right Side
    { classNames: 'smallBgImage13 animate-wiggle-reverse-slow absolute top-[15%] left-[75%]' },
    { classNames: 'smallBgImage14 animate-wiggle-reverse absolute top-[30%] left-[80%]' },
    { classNames: 'smallBgImage15 animate-wiggle-reverse absolute top-[45%] left-[85%]' },
    { classNames: 'smallBgImage16 animate-wiggle-slow absolute top-[60%] left-[70%]' },
    { classNames: 'smallBgImage17 animate-wiggle absolute top-[75%] left-[85%]' },
    { classNames: 'smallBgImage18 animate-wiggle-reverse-slow absolute top-[90%] left-[65%]' },
  ];

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private notificationService: NotificationService,
    protected dialog: MatDialog,
    private backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);

    this.loginForm = this.formBuilder.group({
      usernameOrEmail: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.backgroundService.setClasses(['pinkStars']);
  }

  isValidEmail(email: string): boolean {
    const emailControl = this.formBuilder.control(email, Validators.email);
    return emailControl.valid;
  }

  onSubmit() {
    if (this.loginForm.valid) {
      const usernameOrEmail = this.loginForm.get('usernameOrEmail')?.value;

      const credentials: LoginCredentials = {
        username: undefined,
        email: undefined,
        password: this.loginForm.get('password')?.value,
      };

      if (this.isValidEmail(usernameOrEmail)) {
        credentials.email = usernameOrEmail;
      } else {
        credentials.username = usernameOrEmail;
      }

      this.authService.login(credentials).subscribe({
        next: () => { this.notificationService.popSuccessToast('Login successful', NotificationAction.GO_TO_HOME); },
        error: error => {
          if (error.error == 'Bad credentials') {
            this.notificationService.popErrorToast('Bad login or password');
          } else {
            this.notificationService.popErrorToast('Login failed', error);
          }
        }
      });
    }
  }

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }

  openResetPasswordConfirmationDialog() {
    const dialogRef = this.dialog.open(ResetPasswordConfirmationDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.requestResetPassword(dialogRef);
      }
    });
  }

  requestResetPassword(dialogRef: MatDialogRef<ResetPasswordConfirmationDialogComponent>) {
    if (!dialogRef || !dialogRef.componentRef) {
      return;
    }

    const email = dialogRef.componentRef.instance.resetPasswordForm.get('email')?.value;
    this.authService.requestPasswordReset(email).subscribe();

    this.notificationService.popSuccessToast('Password reset sent');
  }
}
