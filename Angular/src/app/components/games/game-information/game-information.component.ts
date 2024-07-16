import { Component, OnInit } from '@angular/core';
import { Game } from '../../../interfaces/game';
import { ActivatedRoute, Router } from '@angular/router';
import { GameService } from '../../../services/game.service';
import { Location } from '@angular/common';
import { UserReviewService } from '../../../services/user-review.service';
import { UserReview } from '../../../interfaces/userReview';
import { Observer } from 'rxjs';
import { Toast, ToasterService } from 'angular-toaster';
import { UserReviewDeletionConfirmationDialogComponent } from '../../user-reviews/user-review-deletion-confirmation-dialog/user-review-deletion-confirmation-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { formatDate } from '../../../util/formatDate';

@Component({
  selector: 'app-game-information',
  templateUrl: './game-information.component.html',
  styleUrl: './game-information.component.css'
})
export class GameInformationComponent implements OnInit {
  reviewList: UserReview[] = [];
  formatDate = formatDate;

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
    public dialog: MatDialog,
  ) {
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['name']) {
        const gameTitle = params['name'].replace(' ', '-');
        this.gameService.getGameByName(gameTitle).subscribe((game: Game) => {
          this.game = game;

          this.userReviewService.getUserReviewsForGame(gameTitle).subscribe((userReviews: UserReview[]) => {
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
    const dialogRef = this.dialog.open(UserReviewDeletionConfirmationDialogComponent);

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteReview(review);
      }
    });
  }

  deleteReview(review: UserReview) {
    if (review.id) {
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
          console.log("complete")
          this.reviewList = this.reviewList.filter(r => r.id !== review.id);
        }
      };
      this.userReviewService.deleteUserReview(review.id).subscribe(observerTag);
    }
  }
}
