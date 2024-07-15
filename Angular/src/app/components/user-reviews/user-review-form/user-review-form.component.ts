import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observer } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Toast, ToasterService } from 'angular-toaster';
import { UserReview } from '../../../interfaces/userReview';
import { UserReviewService } from '../../../services/user-review.service';
import { GameService } from '../../../services/game.service';
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
    game: undefined,
    user: this.ourUser,
    content: '',
    postDate: new Date(),
    score: undefined,
  };

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private toasterService: ToasterService,
    private userReviewService: UserReviewService,
    private authService: AuthService,
    private gameService: GameService,
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

    if (this.userReview.user) {
      this.userReview.user.username = this.authService.getUserName();
    }
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['name']) {
        this.gameService.getGameByName(params['name']).subscribe((game: Game) => {
          this.userReview.game = game;
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

      if (this.isEditRoute) {
        const observer: Observer<any> = {
          next: response => {
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
        //this.gameService.editGame(this.gameTitle, reviewData).subscribe(observer);
        return;
      }

      const observer: Observer<any> = {
        next: response => {
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
