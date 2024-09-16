import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../../services/auth.service';
import { NotificationService } from '../../../../services/notification.service';
import { NewCredentials } from '../../../../interfaces/newCredentials';
import { passwordMatchValidator } from '../../../../util/passwordMatchValidator';

@Component({
  selector: 'app-password-change',
  templateUrl: './password-change-component.html',
})
export class PasswordChangeComponent {
  protected changePasswordForm: FormGroup;

  protected hideCurrentPassword = signal(true);
  protected hideNewPassword = signal(true);
  protected hideConfirmNewPassword = signal(true);

  protected passwordMismatchErrorMessage = signal('');

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

      if (!userName) {
        this.notificationService.popErrorToast('Password change failed', "Username not found");
        return;
      }

      const newData: NewCredentials = {
        username: userName,
        currentPassword: this.changePasswordForm.get('currentPassword')?.value,
        newPassword: this.changePasswordForm.get('password')?.value,
      };

      this.authService.changeProfile(newData).subscribe({
        next: () => { this.notificationService.popSuccessToast('Password change successful!'); },
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

  hideCurrentPasswordClickEvent(event: MouseEvent) {
    console.log(this.changePasswordForm);

    this.hideCurrentPassword.set(!this.hideCurrentPassword());
    event.stopPropagation();
  }

  hideNewPasswordClickEvent(event: MouseEvent) {
    this.hideNewPassword.set(!this.hideNewPassword());
    event.stopPropagation();
  }

  hideConfirmNewPasswordClickEvent(event: MouseEvent) {
    this.hideConfirmNewPassword.set(!this.hideConfirmNewPassword());
    event.stopPropagation();
  }
}
