import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { trimmedValidator } from '../../../validators/trimmedValidator';
import { ForumCommentService } from '../../../services/forumComment.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-forum-comment-edit-dialog',
  templateUrl: './forum-comment-edit-dialog.component.html',
})
export class ForumCommentEditDialogComponent {
  protected commentForm: FormGroup;
  protected content: string = '';
  public minLength: number = 4;
  
  protected quillToolbarOptions = [
    ['bold', 'italic', 'underline', 'strike'],
    [{ 'color': [] }, { 'background': [] }],
    ['clean']
  ];

  constructor(
    private forumCommentService: ForumCommentService,
    private notificationService: NotificationService,
    protected dialogRef: MatDialogRef<ForumCommentEditDialogComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) protected data: {
      commentId: number,
      commentContent: string
    }
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

  submitComment(): void {
    if (this.commentForm.valid) {
      const forumComment = {
        id: this.data.commentId,
        content: this.commentForm.get('content')?.value,
      };

      this.forumCommentService.editComment(forumComment).subscribe({
        next: () => { this.notificationService.popSuccessToast('Comment edited'); },
        error: error => this.notificationService.popErrorToast('Comment editing failed', error)
      });
    }
  }

  onConfirm(): void {
    this.submitComment();
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
