import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Forum } from '../../../interfaces/forum';
import { NotificationService } from '../../../services/notification.service';
import { ForumPostService } from '../../../services/forumPost.service';
import { ForumPost } from '../../../interfaces/forumPost';
import { ImageCacheService } from '../../../services/imageCache.service';
import { Observer } from 'rxjs';

@Component({
  selector: 'app-forum-post-form-dialog',
  templateUrl: './forum-post-form-dialog.component.html',
})
export class ForumPostFormDialogComponent {
  public forumPostForm: FormGroup;
  public titleMinLength: number = 4;
  public contentMinLength: number = 8;
  public forumList: Forum[] = [];

  private title: string = '';
  private content: string = '';
  private forumId?: number;
  private postId?: number;

  private selectedImage?: File;
  public imageUrl: string = '';
  
  constructor(
    private forumPostService: ForumPostService,
    private notificationService: NotificationService,
    private imageCacheService: ImageCacheService,
    public dialogRef: MatDialogRef<ForumPostFormDialogComponent>,
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
        console.log('Picture: ', this.data.picture);
      }

      if (this.data.forumId) {
        this.forumId = this.data.forumId;
      } else {
        this.forumId = 1;
      }
    }
  }

  loadPostPicture(postId: number, pictureUrl: string) {
    const didProfilePicChange = this.imageCacheService.didPictureNameChange("postPicName" + postId, pictureUrl);

    if (!didProfilePicChange && this.imageCacheService.isCached("postPic" + postId)) {
      const cachedImage = this.imageCacheService.getCachedImage("postPic" + postId);
      if (cachedImage) {
        this.imageUrl = cachedImage;
      }

    } else {
      const observerPicture: Observer<any> = {
        next: response2 => {
          if (response2) {
            this.imageUrl = URL.createObjectURL(response2);
            this.imageCacheService.cacheBlob("postPic" + postId, response2);
            this.imageCacheService.cacheProfilePicName("postPicName" + postId, pictureUrl);
          }
        },
        error: error => {
          console.error(error);
        },
        complete: () => {}
      };
      this.forumPostService.getPicture(postId).subscribe(observerPicture);
    }
  }

  submitForm() {
    if (this.forumPostForm.valid) {
      const newForumPost: ForumPost = {
        title: this.forumPostForm.get('title')?.value,
        content: this.forumPostForm.get('content')?.value,
        forum: this.forumPostForm.get('parentForum')?.value,
      }

      const newForum: Forum = {
        id: this.forumId
      }
      newForumPost.forum = newForum;

      if (this.data && this.data.editing) {
        newForumPost.id = this.postId;
        this.forumPostService.editPost(newForumPost, this.selectedImage).subscribe({
          next: () => { this.notificationService.popSuccessToast('Post edited'); },
          error: error => this.notificationService.popErrorToast('Post editing failed', error)
        });
        return;
      }

      this.forumPostService.addPost(newForumPost, this.selectedImage).subscribe({
        next: () => { this.notificationService.popSuccessToast('Post added'); },
        error: error => this.notificationService.popErrorToast('Post adding failed', error)
      });

    } else {
      console.error('Form is invalid');
    }
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
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
