import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-account-deletion-confirmation-dialog',
  templateUrl: './account-deletion-confirmation-dialog.component.html',
})
export class AccountDeletionConfirmationDialogComponent {
  hidePassword = signal(true);
  public deleteAccountForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<AccountDeletionConfirmationDialogComponent>,
    private formBuilder: FormBuilder,
  ) {
    this.deleteAccountForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }
}
