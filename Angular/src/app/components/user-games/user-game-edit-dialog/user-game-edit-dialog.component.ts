import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CompletionStatus } from '../../../interfaces/completionStatus';
import { UserGame } from '../../../interfaces/userGame';

@Component({
  selector: 'app-user-game-edit-dialog',
  templateUrl: './user-game-edit-dialog.component.html',
})
export class UserGameEditDialogComponent implements OnInit {
  updateForm: FormGroup;
  completionStatus?: CompletionStatus = undefined;
  completionStatuses: CompletionStatus[] = [
    { id: 1, completionName: "Completed" },
    { id: 2, completionName: "In-progress" },
    { id: 3, completionName: "On-hold" },
    { id: 4, completionName: "Planning" },
    { id: 5, completionName: "Dropped" },
  ];
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

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  isCompletionStatusInvalid() {
    const releaseStatusControl = this.updateForm.get('completionStatus');

    if (!releaseStatusControl) {
      return true;
    }

    return releaseStatusControl.hasError('required') && releaseStatusControl.touched
  }
}
