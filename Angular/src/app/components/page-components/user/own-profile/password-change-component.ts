import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../../services/auth.service';
import { NotificationService } from '../../../../services/notification.service';
import { NewCredentials } from '../../../../interfaces/newCredentials';
import { passwordMatchValidator } from '../../../../util/passwordMatchValidator';

@Component({
  selector: 'app-password-change',
  templateUrl: './password-change-component.html',
  styleUrls: [
    '/src/app/styles/shared-form-styles.css',
    './own-profile.component.css'
  ]
})
export class PasswordChangeComponent {
  public changePasswordForm: FormGroup;
  public hidePassword = signal(true);
  public passwordMismatchErrorMessage = signal('');

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private notificationService: NotificationService,
  ) {
    this.changePasswordForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
    }, { validators: passwordMatchValidator });
  }

  onSubmitPasswordChange() {
    if (this.changePasswordForm.valid) {
      const userName = this.authService.getUsername();
      const token = this.authService.getToken();

      if (!userName || !token) {
        this.notificationService.popErrorToast('Password change failed', "Username or token not found");
        return;
      }

      const newData: NewCredentials = {
        username: userName,
        currentPassword: this.changePasswordForm.get('currentPassword')?.value,
        newPassword: this.changePasswordForm.get('password')?.value,
      };

      this.authService.changeProfile(newData, token).subscribe({
        next: () => { this.notificationService.popSuccessToast('Password change successful!', false); },
        error: error => this.notificationService.popErrorToast('Password change failed', error)
      });
    }
  }

  isPasswordMismatch() {
    return this.changePasswordForm.hasError('passwordMismatch');
  }

  updatePasswordMismatchErrorMessage() {
    if (this.isPasswordMismatch()) {
      this.passwordMismatchErrorMessage.set('Passwords do not match');
    } else {
      this.passwordMismatchErrorMessage.set('');
    }
  }

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }
}
