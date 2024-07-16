import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-user-review-deletion-confirmation-dialog',
  templateUrl: './user-review-deletion-confirmation-dialog.component.html',
})
export class UserReviewDeletionConfirmationDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<UserReviewDeletionConfirmationDialogComponent>,
  ) {
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
