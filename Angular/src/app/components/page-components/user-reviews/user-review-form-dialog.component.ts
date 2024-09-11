import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Game } from '../../../interfaces/game';
import { NotificationService } from '../../../services/notification.service';
import { UserReview } from '../../../interfaces/userReview';
import { UserReviewService } from '../../../services/user-review.service';

@Component({
  selector: 'app-user-review-form-dialog',
  templateUrl: './user-review-form-dialog.component.html',
})
export class UserReviewFormDialogComponent {
  protected userReviewForm: FormGroup;
  
  protected userReview: UserReview = {
    gameTitle: '',
    userUsername: '',
    content: '',
    postDate: new Date(),
    score: undefined,
  };

  constructor(
    private formBuilder: FormBuilder,
    private notificationService: NotificationService,
    private userReviewService: UserReviewService,
    protected dialogRef: MatDialogRef<UserReviewFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) protected data: {
      editing: boolean;
      game: Game;
      review: UserReview;
    }
  ) {
    this.userReviewForm = this.formBuilder.group({
      content: [this.userReview.content, [Validators.required, Validators.minLength(10)]],
      score: [this.userReview.score, [Validators.required]]
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
  
  ngOnInit() {
    if (this.data.game) {
      this.userReview.gameTitle = this.data.game.title;
    }

    if (this.data.review) {
      this.userReview = this.data.review;
      this.userReviewForm.setValue({
        content: this.userReview.content,
        score: this.userReview.score
      });
    }
  }

  onSubmit() {
    if (this.userReviewForm.valid) {
      const reviewData = {
        ...this.userReview,
        ...this.userReviewForm.value
      };

      if (this.data.editing) {
        this.userReviewService.editUserReview(reviewData).subscribe({
          next: () => this.notificationService.popSuccessToast('Edited review successfully'),
          error: error => this.notificationService.popErrorToast('Editing review failed', error)
        });
        this.dialogRef.close(true);
        return;
      }

      this.userReviewService.addUserReview(reviewData).subscribe({
        next: () => this.notificationService.popSuccessToast('Added review successfully'),
        error: error => this.notificationService.popErrorToast('Adding review failed', error)
      });
      this.dialogRef.close(true);
    }
  }
}
