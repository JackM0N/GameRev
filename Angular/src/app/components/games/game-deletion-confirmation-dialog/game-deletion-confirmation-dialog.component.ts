import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-game-deletion-confirmation-dialog',
  templateUrl: './game-deletion-confirmation-dialog.component.html',
})
export class GameDeletionConfirmationDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<GameDeletionConfirmationDialogComponent>,
  ) {
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
