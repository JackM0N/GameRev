import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { UserReview } from '../../../interfaces/userReview';
import { UserReviewService } from '../../../services/user-review.service';
import { AuthService } from '../../../services/auth.service';
import { NotificationService } from '../../../services/notification.service';
import { Location } from '@angular/common';

@Component({
  selector: 'app-user-review-form',
  templateUrl: './user-review-form.component.html',
  styleUrl: '/src/app/styles/shared-form-styles.css'
})
export class UserReviewFormComponent implements OnInit {
  userReviewForm: FormGroup;
  isEditRoute: boolean;
  formTitle: string = 'Add a new review';

  userReview: UserReview = {
    gameTitle: '',
    userUsername: '',
    content: '',
    postDate: new Date(),
    score: undefined,
  };

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private notificationService: NotificationService,
    private userReviewService: UserReviewService,
    private authService: AuthService,
    private _location: Location
  ) {
    this.userReviewForm = this.formBuilder.group({
      content: [this.userReview.content, [Validators.required, Validators.minLength(10)]],
      score: [this.userReview.score, [Validators.required]]
    });

    this.isEditRoute = this.route.snapshot.routeConfig?.path?.includes('/edit') == true;

    if (this.isEditRoute) {
      this.formTitle = 'Editing review';
    }

    this.userReview.userUsername = this.authService.getUsername();
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['name']) {
        this.userReview.gameTitle = params['name'];
      }
      
      if (params['id']) {
        this.userReviewService.getUserReviewById(params['id']).subscribe((userReview: UserReview) => {
          this.userReview = userReview;

          this.userReviewForm.setValue({
            content: userReview.content,
            score: userReview.score
          });
        });
      }
    });
  }

  onSubmit() {
    if (this.userReviewForm.valid) {
      const reviewData = {
        ...this.userReview,
        ...this.userReviewForm.value
      };

      const token = this.authService.getToken();

      if (token === null) {
        console.log("Token is null");
        return;
      }

      if (this.isEditRoute) {
        this.userReviewService.editUserReview(reviewData, token).subscribe({
          next: () => this.notificationService.popSuccessToast('Edited review successfully', true),
          error: error => this.notificationService.popErrorToast('Editing review failed', error)
        });
        return;
      }

      this.userReviewService.addUserReview(reviewData, token).subscribe({
        next: () => this.notificationService.popSuccessToast('Added review successfully', true),
        error: error => this.notificationService.popErrorToast('Adding review failed', error)
      });
    }
  }

  cancel() {
    this._location.back();
  }
}
