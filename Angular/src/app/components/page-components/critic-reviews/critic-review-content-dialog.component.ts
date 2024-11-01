import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-review-content-dialog',
  templateUrl: './critic-review-content-dialog.component.html',
})
export class CriticReviewContentDialogComponent {
  constructor(
    private dialogRef: MatDialogRef<CriticReviewContentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) protected data: {
      dialogTitle: string;
      dialogContent: string;
    }
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
