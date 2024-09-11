import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Game } from '../../../interfaces/game';
import { NotificationService } from '../../../services/notification.service';
import { CriticReview } from '../../../interfaces/criticReview';
import { CriticReviewService } from '../../../services/critic-review.service';

@Component({
  selector: 'app-critic-review-form-dialog',
  templateUrl: './critic-review-form-dialog.component.html',
})
export class CriticReviewFormDialogComponent {
  protected criticReviewForm: FormGroup;
  protected contentMinLength: number = 8;
  
  protected criticReview: CriticReview = {
    gameTitle: '',
    userUsername: '',
    content: '',
    postDate: new Date(),
    score: undefined,
  };

  protected quillToolbarOptions = [
    ['bold', 'italic', 'underline', 'strike'],
    [{ 'color': [] }, { 'background': [] }],
    ['clean']
  ];

  constructor(
    private formBuilder: FormBuilder,
    private notificationService: NotificationService,
    private criticReviewService: CriticReviewService,
    protected dialogRef: MatDialogRef<CriticReviewFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) protected data: {
      editing: boolean;
      gameTitle: string;
      review: CriticReview;
    }
  ) {
    this.criticReviewForm = this.formBuilder.group({
      content: [this.criticReview.content, [Validators.required, Validators.minLength(this.contentMinLength)]],
      score: [this.criticReview.score, [Validators.required]]
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
  
  ngOnInit() {
    if (this.data.review) {
      this.criticReview = this.data.review;
      this.criticReviewForm.setValue({
        content: this.criticReview.content,
        score: this.criticReview.score
      });
    }

    if (this.data.gameTitle) {
      this.criticReview.gameTitle = this.data.gameTitle;
    }
  }

  onSubmit() {
    if (this.criticReviewForm.valid && this.criticReview.gameTitle) {
      const reviewData = {
        ...this.criticReview,
        ...this.criticReviewForm.value
      };

      if (this.data.editing) {
        this.criticReviewService.editCriticReview(reviewData).subscribe({
          next: () => this.notificationService.popSuccessToast('Edited review successfully'),
          error: error => this.notificationService.popErrorToast('Editing review failed', error)
        });
        this.dialogRef.close(true);
        return;
      }

      this.criticReviewService.addCriticReview(reviewData).subscribe({
        next: () => this.notificationService.popSuccessToast('Added review successfully'),
        error: error => this.notificationService.popErrorToast('Adding review failed', error)
      });
      this.dialogRef.close(true);
    }
  }
}
