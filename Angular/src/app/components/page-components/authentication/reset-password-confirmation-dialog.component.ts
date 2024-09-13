import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-reset-password-confirmation-dialog',
  templateUrl: './reset-password-confirmation-dialog.component.html',
})
export class ResetPasswordConfirmationDialogComponent {
  public resetPasswordForm: FormGroup;
  public emailErrorMessage = signal('');

  constructor(
    public dialogRef: MatDialogRef<ResetPasswordConfirmationDialogComponent>,
    private formBuilder: FormBuilder,
  ) {
    this.resetPasswordForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  isEmailInvalid() {
    return this.resetPasswordForm.get('email')?.invalid && this.resetPasswordForm.get('email')?.touched;
  }

  updateEmailErrorMessage() {
    const email = this.resetPasswordForm.get('email');

    if(email != null) {
      if (email.hasError('required')) {
        this.emailErrorMessage.set('Please provide an email address');
  
      } else if (email.hasError('email')) {
        this.emailErrorMessage.set('Not a valid email');
  
      } else {
        this.emailErrorMessage.set('');
      }
    }
  }
}
