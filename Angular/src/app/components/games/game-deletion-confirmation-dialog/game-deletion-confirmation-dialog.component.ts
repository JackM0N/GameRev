import { Component, signal } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-game-deletion-confirmation-dialog',
  templateUrl: './game-deletion-confirmation-dialog.component.html',
})
export class GameDeletionConfirmationDialogComponent {
  hidePassword = signal(true);

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

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }
}
