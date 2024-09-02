import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CompletionStatus } from '../../../interfaces/completionStatus';
import { UserGame } from '../../../interfaces/userGame';
import { completionStatuses } from '../../../enums/completionStatuses';

@Component({
  selector: 'app-library-edit-dialog',
  templateUrl: './library-edit-dialog.component.html',
})
export class LibraryEditDialogComponent implements OnInit {
  updateForm: FormGroup;
  completionStatus?: string = undefined;
  completionStatuses: CompletionStatus[] = completionStatuses;
  isFavourite: boolean = false;
  
  constructor(
    public dialogRef: MatDialogRef<LibraryEditDialogComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: { dialogTitle: string, userGame: UserGame }
  ) {
    this.updateForm = this.formBuilder.group({
      completionStatus: [this.completionStatus, [Validators.required, Validators.minLength(1)]],
      isFavourite: [this.isFavourite, [Validators.required]]
    });
  }

  ngOnInit(): void {
    if (this.data.userGame) {
      this.isFavourite = this.data.userGame.isFavourite;

      this.completionStatuses.forEach((status) => {
        if (status.className == this.data.userGame.completionStatus) {
          this.completionStatus = status.className;
          this.updateForm.patchValue({
            completionStatus: status.className
          });
        }
      });
    }
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  isCompletionStatusInvalid() {
    const completionStatusControl = this.updateForm.get('completionStatus');

    if (!completionStatusControl) {
      return true;
    }

    return completionStatusControl.hasError('required') && completionStatusControl.touched
  }
}
