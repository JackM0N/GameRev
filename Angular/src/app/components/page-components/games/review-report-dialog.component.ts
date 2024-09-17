import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-review-report-dialog',
  templateUrl: './review-report-dialog.component.html',
})
export class ReviewReportDialogComponent {
  public reportForm: FormGroup;
  protected content: string = '';
  protected descriptionMinLength = 8;
  
  constructor(
    protected dialogRef: MatDialogRef<ReviewReportDialogComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) protected data: { dialogContent: string }
  ) {
    this.reportForm = this.formBuilder.group({
      content: [this.content, [Validators.required, Validators.minLength(this.descriptionMinLength)]],
    });
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
