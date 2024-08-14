import { AfterViewInit, Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { UserReviewService } from '../../../../services/user-review.service';
import { AuthService } from '../../../../services/auth.service';
import { ReportService } from '../../../../services/report.service';
import { Router } from '@angular/router';
import { UserReview } from '../../../../interfaces/userReview';
import { Observer } from 'rxjs';
import { ReviewReportDialogComponent } from '../review-report-dialog.component';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { PopupDialogComponent } from '../../../general-components/popup-dialog.component';
import { Game } from '../../../../interfaces/game';
import { Report } from '../../../../interfaces/report';
import { formatDate } from '../../../../util/formatDate';
import { NotificationService } from '../../../../services/notification.service';

@Component({
  selector: 'app-gameinfo-review-list',
  templateUrl: './review-list.component.html',
  styleUrl: './game-information.component.css'
})
export class GameInfoReviewListComponent implements AfterViewInit {
  @Output() usersScoreUpdated = new EventEmitter<number>();

  @Input() gameTitle?: string;
  @Input() game?: Game;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  reviewList: UserReview[] = [];
  totalReviews: number = 0;
  dataSource: MatTableDataSource<UserReview> = new MatTableDataSource<UserReview>(this.reviewList);
  formatDate = formatDate;

  report: Report = {
    content: '',
    userReview: {
      id: -1
    }
  }

  constructor(
    private userReviewService: UserReviewService,
    private notificationService: NotificationService,
    private authService: AuthService,
    private reportService: ReportService,
    private router: Router,
    public dialog: MatDialog,
  ) {}

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.loadReviews();
    this.paginator.page.subscribe(() => this.loadReviews());
  }

  loadReviews() {
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    if (this.gameTitle === undefined) {
      console.log("Game title is undefined");
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
      const username = this.authService.getUsername();

      if (token === null) {
        console.log("Token is null");
        return;
      }

      if (username === null) {
        console.log("Username is null");
        return;
      }

      review.userUsername = username;

      this.userReviewService.deleteUserReview(review, token).subscribe({
        next: () => {
          this.reviewList = this.reviewList.filter(r => r.id !== review.id);
          this.usersScoreUpdated.emit(review.score);
          this.notificationService.popSuccessToast('Deleted review successfuly', false);
        },
        error: error => this.notificationService.popErrorToast('Deleting review failed', error)
      });
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

    this.reportService.reportReview(this.report, token).subscribe({
      next: () => { this.notificationService.popSuccessToast('Report sent successfully', false); },
      error: error => this.notificationService.popErrorToast('Report submission failed', error)
    });
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

  canDeleteReview(review: UserReview) {
    return this.authService.isAuthenticated() &&
    (this.authService.getUsername() === review.userUsername || this.authService.hasAnyRole(['Admin', 'Critic']));
  }

  ownsReview(review: UserReview) {
    return this.authService.isAuthenticated() && this.authService.getUsername() === review.userUsername;
  }

  canAddReview() {
    if (!this.authService.isAuthenticated()) {
      return false;
    }

    const userName = this.authService.getUsername();
    return !this.reviewList.some(review => review.userUsername === userName);
  }

  routeToAddNewReview() {
    if (this.game) {
      this.router.navigate(['/user-reviews/add/' + this.game.title]);
    }
  }

  editReview(review: UserReview) {
    this.router.navigate(['/user-reviews/edit/' + review.id]);
  }
}
