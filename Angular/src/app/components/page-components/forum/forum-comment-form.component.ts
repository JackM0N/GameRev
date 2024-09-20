import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NotificationService } from '../../../services/notification.service';
import { ForumCommentService } from '../../../services/forumComment.service';
import { AuthService } from '../../../services/auth.service';
import { trimmedValidator } from '../../../validators/trimmedValidator';

@Component({
  selector: 'app-forum-comment-form',
  templateUrl: './forum-comment-form.component.html',
})
export class ForumCommentFormComponent {
  protected commentForm: FormGroup;
  protected minLength: number = 4;
  @Input() public postId: any;
  @Output() public commentPosted = new EventEmitter<void>();
  
  protected quillToolbarOptions = [
    ['bold', 'italic', 'underline', 'strike'],
    [{ 'color': [] }, { 'background': [] }],
    ['clean']
  ];

  constructor(
    protected authService: AuthService,
    private notificationService: NotificationService,
    private forumCommentService: ForumCommentService,
    private fb: FormBuilder
  ) {
    this.commentForm = this.fb.group({
      content: [{value: '', disabled: !authService.isAuthenticated()}, [Validators.required, Validators.minLength(this.minLength), trimmedValidator(this.minLength)]]
    });
  }

  submitComment() {
    if (true) {
      console.log(this.commentForm);
      return;
    }

    if (this.commentForm.valid && this.postId) {
      const newComment = {
        content: this.commentForm.value.content,
        forumPostId: this.postId
      };

      this.forumCommentService.addComment(newComment).subscribe({
        next: () => {
          this.notificationService.popSuccessToast('Comment posted successfully');
          this.commentPosted.emit();
          this.commentForm.reset();
        },
        error: error => this.notificationService.popErrorToast('Comment posting failed', error)
      });
    }
  }
}
