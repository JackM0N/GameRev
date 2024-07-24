import { Component, OnInit } from '@angular/core';
import { Game } from '../../../interfaces/game';
import { ActivatedRoute, Router } from '@angular/router';
import { GameService } from '../../../services/game.service';
import { Location } from '@angular/common';
import { UserReviewService } from '../../../services/user-review.service';
import { UserReview } from '../../../interfaces/userReview';
import { Observer } from 'rxjs';
import { Toast, ToasterService } from 'angular-toaster';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { formatDate } from '../../../util/formatDate';
import { AuthService } from '../../../services/auth.service';
import { PopupDialogComponent } from '../../popup-dialog/popup-dialog.component';
import { ReviewReportDialogComponent } from '../review-report-dialog/review-report-dialog.component';
import { Report } from '../../../interfaces/report';

@Component({
  selector: 'app-game-information',
  templateUrl: './game-information.component.html',
  styleUrl: './game-information.component.css'
})
export class GameInformationComponent implements OnInit {
  reviewList: UserReview[] = [];
  formatDate = formatDate;
  likeColor: 'primary' | '' = '';
  dislikeColor: 'warn' | '' = '';
  usersScoreText: string = '';

  report: Report = {
    content: '',
    reviewId: 0
  }

  game: Game = {
    title: '',
    developer: '',
    publisher: '',
    releaseDate: [],
    releaseStatus: undefined,
    description: '',
    tags: [],
    usersScore: 0
  };

  constructor(
    private route: ActivatedRoute,
    private gameService: GameService,
    private userReviewService: UserReviewService,
    private router: Router,
    private _location: Location,
    private toasterService: ToasterService,
    private authService: AuthService,
    public dialog: MatDialog,
  ) {
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['name']) {
        const gameTitle = params['name'].replace(' ', '-');
        this.gameService.getGameByName(gameTitle).subscribe((game: Game) => {
          this.game = game;

          this.updateUsersScoreText();

          if (this.game.usersScore > 0) {
            this.usersScoreText = "Users score: " + this.game.usersScore;
          } else {
            this.usersScoreText = "No reviews yet";
          }

          const token = this.authService.getToken();

          if (token === null) {
            console.log("Token is null");
            return;
          }

          this.userReviewService.getUserReviewsForGame(gameTitle, token).subscribe((userReviews: UserReview[]) => {
            this.reviewList = userReviews;
          });
        });
      }
    });
  }

  goBack() {
    this._location.back();
  }

  routeToAddNewReview() {
    this.router.navigate(['/user-reviews/add/' + this.game.title]);
  }

  editReview(review: UserReview) {
    this.router.navigate(['/user-reviews/edit/' + review.id]);
  }

  openReviewDeletionConfirmationDialog(review: UserReview) {
    const dialogTitle = 'Confirm review deletion';
    const dialogContent = 'Are you sure you want to delete review by ' + review.userUsername + '?';
    const submitText = 'Delete';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteReview(review);
      }
    });
  }

  deleteReview(review: UserReview) {
    if (review.id) {
      const token = this.authService.getToken();

      if (token === null) {
        console.log("Token is null");
        return;
      }

      const observerTag: Observer<any> = {
        next: response => {
          var toast: Toast = {
            type: 'success',
            title: 'Deleted review successfuly',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        error: error => {
          console.error(error);
          var toast: Toast = {
            type: 'error',
            title: 'Deleting review failed',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        complete: () => {
          this.reviewList = this.reviewList.filter(r => r.id !== review.id);

          this.updateUsersScoreText(review);
        }
      };
      this.userReviewService.deleteUserReview(review, token).subscribe(observerTag);
    }
  }

  updateUsersScoreText(review?: UserReview) {
    var calculatedScore = this.game.usersScore;

    if (review && review.score) {
      calculatedScore = calculatedScore - review.score;
    }

    if (calculatedScore > 0) {
      this.usersScoreText = "Users score: " + calculatedScore;
    } else {
      this.usersScoreText = "No reviews yet";
    }
  }

  toggleLike(review: UserReview) {
    if (review.ownRatingIsPositive === true) {
      review.ownRatingIsPositive = undefined;
      if (review.positiveRating != undefined) {
        review.positiveRating--;
      }

    } else {
      review.ownRatingIsPositive = true;
      if (review.positiveRating != undefined) {
        review.positiveRating++;
      }
    }

    this.sendRatingInformation(review);
  }

  toggleDislike(review: UserReview) {
    if (review.ownRatingIsPositive === false) {
      review.ownRatingIsPositive = undefined;
      if (review.negativeRating != undefined) {
        review.negativeRating--;
      }

    } else {
      review.ownRatingIsPositive = false;
      if (review.negativeRating != undefined) {
        review.negativeRating++;
      }
    }

    this.sendRatingInformation(review);
  }

  sendRatingInformation(review: UserReview) {
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    const observer: Observer<any> = {
      next: response => {
      },
      error: error => {
        console.error(error);
      },
      complete: () => {
      }
    };
    this.userReviewService.updateUserReviewRating(review, token).subscribe(observer);
  }

  sendReportInformation(review: UserReview, dialogRef: MatDialogRef<ReviewReportDialogComponent>) {
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    if (!dialogRef || !dialogRef.componentRef) {
      return;
    }

    if (review.id) {
      this.report.reviewId = review.id;
    }

    const content = dialogRef.componentRef.instance.reportForm.get('content')?.value;

    if (content) {
      this.report.content = content;
    } else {
      console.log("Content is null");
      return;
    }

    console.log(this.report);

    const observer: Observer<any> = {
      next: response => {
        var toast: Toast = {
          type: 'success',
          title: 'Report sent successfully',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'Report submission failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {
      }
    };
    this.userReviewService.reportReview(this.report, token).subscribe(observer);
  }

  reportReview(review: UserReview) {
    const dialogContent = 'Are you sure you want to report this review by ' + review.userUsername + '?';

    const dialogRef = this.dialog.open(ReviewReportDialogComponent, {
      width: '300px',
      data: { dialogContent }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.sendReportInformation(review, dialogRef);
      }
    });
  }
}
