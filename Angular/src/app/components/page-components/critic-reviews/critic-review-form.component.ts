import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observer } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { Location } from '@angular/common';
import { CriticReview } from '../../../interfaces/criticReview';
import { CriticReviewService } from '../../../services/critic-review.service';
import { BackgroundService } from '../../../services/background.service';
import { NotificationService } from '../../../services/notification.service';

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

  quillToolbarOptions = [
    ['bold', 'italic', 'underline', 'strike'],
    [{ 'color': [] }, { 'background': [] }],
    ['clean']
  ];

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private notificationService: NotificationService,
    private criticReviewService: CriticReviewService,
    private authService: AuthService,
    private _location: Location,
    private backgroundService: BackgroundService,
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
    //this.backgroundService.setMainContentStyle({'padding-left': '200px'});

    this.route.params.subscribe(params => {
      if (params['name']) {
        this.criticReview.gameTitle = params['name'];
      }

      if (params["id"] && this.isEditRoute) {
        const observer: Observer<any> = {
          next: response => {
            if (response) {
              this.criticReview = response;

              this.criticReviewForm.setValue({
                content: response.content,
                score: response.score
              });
            }
          },
          error: () => {},
          complete: () => {}
        };
        this.criticReviewService.getCriticReviewById(params['id']).subscribe(observer);
      }
    });
  }
 
  onSubmit() {
    if (this.criticReviewForm.valid) {
      const reviewData = {
        ...this.criticReview,
        ...this.criticReviewForm.value
      };

      if (this.isEditRoute) {
        this.criticReviewService.editCriticReview(reviewData).subscribe({
          next: () => { this.notificationService.popSuccessToast('Edited review successfuly', true); },
          error: error => this.notificationService.popErrorToast('Editing review failed', error)
        });
        return;
      }

      this.criticReviewService.addCriticReview(reviewData).subscribe({
        next: () => { this.notificationService.popSuccessToast('Added review successfuly', true); },
        error: error => this.notificationService.popErrorToast('Adding review failed', error)
      });
    }
  }

  cancel() {
    this._location.back();
  }
}
