import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observer } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { Toast, ToasterService } from 'angular-toaster';
import { AuthService } from '../../../services/auth.service';
import { Location } from '@angular/common';
import { CriticReview } from '../../../interfaces/criticReview';
import { CriticReviewService } from '../../../services/critic-review.service';

@Component({
  selector: 'app-critic-review-form',
  templateUrl: './critic-review-form.component.html',
  styleUrl: '/src/app/styles/shared-form-styles.css'
})
export class CriticReviewFormComponent implements OnInit {
  criticReviewForm: FormGroup;
  isEditRoute: boolean;
  formTitle: string = 'Add a new critic review';

  criticReview: CriticReview = {
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
    private criticReviewService: CriticReviewService,
    private authService: AuthService,
    private _location: Location
  ) {
    this.criticReviewForm = this.formBuilder.group({
      content: [this.criticReview.content, [Validators.required, Validators.minLength(10)]],
      score: [this.criticReview.score, [Validators.required]]
    });

    this.isEditRoute = this.route.snapshot.routeConfig?.path?.includes('/edit') == true;

    if (this.isEditRoute) {
      this.formTitle = 'Editing critic review';
    }

    this.criticReview.userUsername = this.authService.getUsername();
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['name']) {
        this.criticReview.gameTitle = params['name'];

        if (this.isEditRoute) {
          const observer: Observer<any> = {
            next: response => {
              console.log(response);
              if (response && response.length > 0) {
                this.criticReview = response[0];
  
                this.criticReviewForm.setValue({
                  content: response[0].content,
                  score: response[0].score
                });
              }
            },
            error: error => {
            },
            complete: () => {}
          };
          this.criticReviewService.getCriticReviewsByGameTitle(params['name']).subscribe(observer);
        }
      }
    });
  }
 
  onSubmit() {
    if (this.criticReviewForm.valid) {
      const reviewData = {
        ...this.criticReview,
        ...this.criticReviewForm.value
      };

      const token = this.authService.getToken();

      if (token === null) {
        console.log("Token is null");
        return;
      }

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
        this.criticReviewService.editCriticReview(reviewData, token).subscribe(observer);
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
      this.criticReviewService.addCriticReview(reviewData, token).subscribe(observer);
    }
  }

  cancel() {
    this._location.back();
  }
}
