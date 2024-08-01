import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CompletionStatus } from '../../../interfaces/completionStatus';
import { UserGame } from '../../../interfaces/userGame';
import { completionStatuses } from '../../../interfaces/completionStatuses';

@Component({
  selector: 'app-user-game-edit-dialog',
  templateUrl: './user-game-edit-dialog.component.html',
})
export class UserGameEditDialogComponent implements OnInit {
  updateForm: FormGroup;
  completionStatus?: CompletionStatus = undefined;
  completionStatuses: CompletionStatus[] = completionStatuses;
  isFavourite: boolean = false;
  
  constructor(
    public dialogRef: MatDialogRef<UserGameEditDialogComponent>,
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
        if (status.completionName === this.data.userGame.completionStatus.completionName) {
          this.completionStatus = status;
          this.updateForm.patchValue({
            completionStatus: status
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