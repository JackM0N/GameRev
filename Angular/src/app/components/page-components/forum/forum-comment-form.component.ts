import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NotificationService } from '../../../services/notification.service';
import { ForumCommentService } from '../../../services/forumComment.service';
import { AuthService } from '../../../services/auth.service';
import { quillTextLengthValidator } from '../../../util/quillTextLengthValidator';
import { FileUploadOptions } from '../../../enums/fileUploadOptions';
//import Quill from 'quill';
//import ImageResizor from 'quill-image-resizor';
//Quill.register('modules/imageResizor', ImageResizor)

@Component({
  selector: 'app-forum-comment-form',
  templateUrl: './forum-comment-form.component.html',
})
export class ForumCommentFormComponent {
  protected commentForm: FormGroup;
  protected minLength: number = 4;
  @Input() public postId: any;
  @Output() public commentPosted = new EventEmitter<void>();

  protected imageUrl: string = '';
  private selectedImage?: File;
  
  protected modules = {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      ['clean'],
      ['image', 'link']
    ],
    imageResizor: {},
  };

  constructor(
    protected authService: AuthService,
    private notificationService: NotificationService,
    private forumCommentService: ForumCommentService,
    private formBuilder: FormBuilder
  ) {
    this.commentForm = this.formBuilder.group({
      content: [{value: '', disabled: !authService.isAuthenticated()}, [Validators.required, quillTextLengthValidator(this.minLength)]]
    });
  }

  submitComment() {
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

  maxSizeError() {
    this.notificationService.popErrorToast('Image size too large. Max size is 10MB.');
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];

    if (file && file.size > FileUploadOptions.MAX_FILE_SIZE) {
      this.maxSizeError();
      return;
    }

    if (file) {
      this.selectedImage = file;
      this.imageUrl = URL.createObjectURL(this.selectedImage);
    }
  }
}

