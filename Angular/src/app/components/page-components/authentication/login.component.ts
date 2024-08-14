import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { LoginCredentials } from '../../../interfaces/loginCredentials';
import { Router } from '@angular/router';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { BackgroundService } from '../../../services/background.service';
import { ResetPasswordConfirmationDialogComponent } from './reset-password-confirmation-dialog.component';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: [
    '/src/app/styles/shared-form-styles.css',
  ]
})
export class LoginComponent {
  loginForm: FormGroup;
  hidePassword = signal(true);

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router,
    public dialog: MatDialog,
    private backgroundService: BackgroundService
  ) {
    this.loginForm = this.formBuilder.group({
      usernameOrEmail: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  ngOnInit(): void {
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
        next: () => {
          this.router.navigate(['/']);
          this.notificationService.popSuccessToast('Login successful', false);
        },
        error: error => this.notificationService.popErrorToast('Login failed', error)
      });
    }
  }

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }

  openResetPasswordConfirmationDialog() {
    const dialogRef = this.dialog.open(ResetPasswordConfirmationDialogComponent, {
      width: '300px'
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

    this.notificationService.popSuccessToast('Password reset sent', false);
  }
}
