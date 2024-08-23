import { Component, Inject } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { trimmedValidator } from '../../../validators/trimmedValidator';

@Component({
  selector: 'app-forum-comment-edit-dialog',
  templateUrl: './forum-comment-edit-dialog.component.html',
})
export class ForumCommentEditDialogComponent {
  commentForm: FormGroup;
  content: string = '';
  public minLength: number = 4;
  
  constructor(
    public dialogRef: MatDialogRef<ForumCommentEditDialogComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: { commentContent: string }
  ) {
    this.commentForm = this.formBuilder.group({
      content: [this.content, [Validators.required, Validators.minLength(this.minLength), trimmedValidator(this.minLength)]],
    });
  }

  ngOnInit(): void {
    this.content = this.data.commentContent;
    this.commentForm.patchValue({
      content: this.data.commentContent
    });
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
