import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Forum } from '../../../models/forum';
import { NotificationService } from '../../../services/notification.service';
import { ForumPostService } from '../../../services/forumPost.service';
import { ForumPost } from '../../../models/forumPost';
import { FileUploadOptions } from '../../../enums/fileUploadOptions';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-forum-post-form-dialog',
  templateUrl: './forum-post-form-dialog.component.html',
})
export class ForumPostFormDialogComponent implements OnInit {
  protected forumPostForm: FormGroup;
  protected titleMinLength = 4;
  protected contentMinLength = 8;
  protected forumList: Forum[] = [];

  private title = '';
  private content = '';
  private forumId?: number;
  private postId?: number;

  private selectedImage?: File;
  protected imageUrl = '';
  
  protected quillToolbarOptions = [
    ['bold', 'italic', 'underline', 'strike'],
    [{ 'color': [] }, { 'background': [] }],
    ['clean']
  ];

  constructor(
    private forumPostService: ForumPostService,
    private notificationService: NotificationService,
    protected dialogRef: MatDialogRef<ForumPostFormDialogComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data?: {
      id?: number,
      content?: string,
      title?: string,
      forumId?: number,
      editing?: boolean,
      picture?: string
    }
  ) {
    this.forumPostForm = this.formBuilder.group({
      title: [this.title, [Validators.required, Validators.minLength(this.titleMinLength)]],
      content: [this.content, [Validators.required, Validators.minLength(this.contentMinLength)]]
    });
  }

  ngOnInit(): void {
    if (this.data) {
      if (this.data.id) {
        this.postId = this.data.id;
      }

      if (this.data.title) {
        this.title = this.data.title;
        this.forumPostForm.patchValue({
          title: this.data.title
        });
      }

      if (this.data.content) {
        this.content = this.data.content;
        this.forumPostForm.patchValue({
          content: this.data.content
        });
      }

      if (this.data.picture) {
        this.imageUrl = this.data.picture;
      }

      if (this.data.forumId) {
        this.forumId = this.data.forumId;
      } else {
        this.forumId = 1;
      }
    }
  }

  maxSizeError() {
    this.notificationService.popErrorToast('Image size too large. Max size is 10MB.');
  }

  checkError(error: HttpErrorResponse) {
    if (error.error == "Maximum upload size exceeded") {
      this.maxSizeError();
      
    } else if (this.data?.editing) {
      this.notificationService.popErrorToast('Post editing failed', error);

    } else {
      this.notificationService.popErrorToast('Post adding failed', error);
    }
  }

  submitForm() {
    if (this.forumPostForm.valid) {
      const newForumPost: ForumPost = {
        title: this.forumPostForm.get('title')?.value,
        content: this.forumPostForm.get('content')?.value,
        forum: this.forumPostForm.get('parentForum')?.value,
      }

      if (this.selectedImage && this.selectedImage.size > FileUploadOptions.MAX_FILE_SIZE) {
        this.maxSizeError();
        return;
      }

      const newForum: Forum = {
        id: this.forumId
      }
      newForumPost.forum = newForum;

      if (this.data && this.data.editing) {
        newForumPost.id = this.postId;
        this.forumPostService.editPost(newForumPost, this.selectedImage).subscribe({
          next: () => { this.notificationService.popSuccessToast('Post edited'); },
          error: error => { this.checkError(error); }
        });
        return;
      }

      this.forumPostService.addPost(newForumPost, this.selectedImage).subscribe({
        next: () => { this.notificationService.popSuccessToast('Post added'); },
        error: error => { this.checkError(error); }
      });

    } else {
      console.error('Form is invalid');
    }
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

  onConfirm(): void {
    this.dialogRef.close(true);
    this.submitForm();
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
