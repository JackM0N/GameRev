import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observer } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { Toast, ToasterService } from 'angular-toaster';
import { UserReview } from '../../../interfaces/userReview';
import { UserReviewService } from '../../../services/user-review.service';
import { Game } from '../../../interfaces/game';
import { WebsiteUser } from '../../../interfaces/websiteUser';
import { AuthService } from '../../../services/auth.service';
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
  gamesList: Game[] = [];

  ourUser: WebsiteUser = {
    username: '',
  }

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
    private toasterService: ToasterService,
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

    this.userReview.userUsername = this.authService.getUserName();
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

      reviewData.token = this.authService.getToken();

      if (this.isEditRoute) {
        const observer: Observer<any> = {
          next: response => {
            this._location.back();
            var toast: Toast = {
              type: 'success',
              title: 'Edited review successfuly',
              showCloseButton: true
            };
            this.toasterService.pop(toast);
          },
          error: error => {
            console.error(error);
            var toast: Toast = {
              type: 'error',
              title: 'Editing review failed',
              showCloseButton: true
            };
            this.toasterService.pop(toast);
          },
          complete: () => {}
        };
        this.userReviewService.editUserReview(reviewData).subscribe(observer);
        return;
      }

      const observer: Observer<any> = {
        next: response => {
          this._location.back();
          var toast: Toast = {
            type: 'success',
            title: 'Added review successfuly',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        error: error => {
          console.error(error);
          var toast: Toast = {
            type: 'error',
            title: 'Adding review failed',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        complete: () => {}
      };
      this.userReviewService.addUserReview(reviewData).subscribe(observer);
    }
  }

  cancel() {
    this._location.back();
  }
}
