import { ChangeDetectorRef, Component, signal } from '@angular/core';
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
export class LoginComponent extends BaseAdComponent {
  protected loginForm: FormGroup;
  protected hidePassword = signal(true);

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
            this.notificationService.popErrorToast('Bad login or password', error);
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
