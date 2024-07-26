import { Component, OnInit, ViewChild } from '@angular/core';
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
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ReportService } from '../../../services/report.service';

@Component({
  selector: 'app-game-information',
  templateUrl: './game-information.component.html',
  styleUrl: './game-information.component.css'
})
export class GameInformationComponent implements OnInit {
  formatDate = formatDate;
  likeColor: 'primary' | '' = '';
  dislikeColor: 'warn' | '' = '';
  usersScoreText: string = '';
  
  reviewList: UserReview[] = [];
  totalReviews: number = 0;
  dataSource: MatTableDataSource<UserReview> = new MatTableDataSource<UserReview>(this.reviewList);
  gameTitle: string = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  report: Report = {
    content: '',
    userReview: {
      id: -1
    }
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
    private toasterService: ToasterService,
    private authService: AuthService,
    private reportService: ReportService,
    private router: Router,
    private _location: Location,
    public dialog: MatDialog,
  ) {
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['name']) {
        this.gameTitle = params['name'].replace(' ', '-');

        this.gameService.getGameByName(this.gameTitle).subscribe((game: Game) => {
          this.game = game;

          this.updateUsersScoreText();

          if (this.game.usersScore > 0) {
            this.usersScoreText = "Users score: " + this.game.usersScore;
          } else {
            this.usersScoreText = "No reviews yet";
          }
        });
      }
    });
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;

    this.loadReviews();

    this.paginator.page.subscribe(() => this.loadReviews());
    /*
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadReviews();
    });
    */
  }

  loadReviews() {
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = 'id';
    const sortDir = 'asc';

    const observer: Observer<any> = {
      next: response => {
        this.reviewList = response.content;
        this.totalReviews = response.totalElements;
        this.dataSource = new MatTableDataSource<UserReview>(this.reviewList);
        this.dataSource.data = this.reviewList;
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.userReviewService.getUserReviewsForGame(this.gameTitle, token, page, size, sortBy, sortDir).subscribe(observer);
  }

  sortData(sort: Sort) {
    this.loadReviews();
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
      this.report.userReview.id = review.id;
    }

    const content = dialogRef.componentRef.instance.reportForm.get('content')?.value;

    if (content) {
      this.report.content = content;
    } else {
      console.log("Content is null");
      return;
    }

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
    this.reportService.reportReview(this.report, token).subscribe(observer);
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
