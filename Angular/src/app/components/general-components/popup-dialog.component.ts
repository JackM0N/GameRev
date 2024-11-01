import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { PopupDialogData } from '../../models/popupDialogData';

@Component({
  selector: 'app-popup-dialog',
  templateUrl: './popup-dialog.component.html',
})
export class PopupDialogComponent implements OnInit {
  @Input() dialogTitle = '';
  @Input() dialogContent = '';
  @Input() submitText = 'Submit';
  @Input() cancelText = 'Cancel';
  @Input() noSubmitButton = false;
  @Input() cancelColor = '';
  @Input() submitColor = '';
  @Input() submitDisabled = false;
  
  @Output() submitted = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  constructor(
    private dialogRef: MatDialogRef<PopupDialogComponent>,
    @Inject(MAT_DIALOG_DATA) protected injectedData: PopupDialogData
  ) {}

  ngOnInit(): void {
    if (this.injectedData) {
      this.dialogTitle = this.injectedData.dialogTitle || this.dialogTitle;
      this.dialogContent = this.injectedData.dialogContent || this.dialogContent;
      this.submitText = this.injectedData.submitText || this.submitText;
      this.cancelText = this.injectedData.cancelText || this.cancelText;
      this.noSubmitButton = this.injectedData.noSubmitButton || this.noSubmitButton;
      this.cancelColor = this.injectedData.cancelColor || this.cancelColor;
      this.submitColor = this.injectedData.submitColor || this.submitColor;
      this.submitDisabled = this.injectedData.submitDisabled || this.submitDisabled;
    }
  }

  onConfirm(): void {
    if (!this.submitDisabled) {
      this.submitted.emit();
      if (this.dialogRef) {
        this.dialogRef.close(true);
      }
    }
  }

  onCancel(): void {
    this.cancelled.emit();
    if (this.dialogRef) {
      this.dialogRef.close(false);
    }
  }
}
