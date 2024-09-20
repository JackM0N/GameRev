import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-popup-dialog',
  templateUrl: './popup-dialog.component.html',
})
export class PopupDialogComponent implements OnInit {
  constructor(
    private dialogRef: MatDialogRef<PopupDialogComponent>,
    @Inject(MAT_DIALOG_DATA) protected data: {
      dialogTitle: string;
      dialogContent: string;
      submitText: string;
      cancelText: string;
      noSubmitButton?: boolean;
      submitColor: string;
    }
  ) {}

  ngOnInit(): void {
    this.data.submitText = this.data.submitText || 'Submit';
    this.data.cancelText = this.data.cancelText || 'Cancel';
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
