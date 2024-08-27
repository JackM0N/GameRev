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
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { PopupDialogComponent } from '../../../general-components/popup-dialog.component';
import { Game } from '../../../../interfaces/game';
import { Report } from '../../../../interfaces/report';
import { formatDate } from '../../../../util/formatDate';
import { NotificationService } from '../../../../services/notification.service';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { DatePipe } from '@angular/common';
import { reviewFilters } from '../../../../interfaces/reviewFilters';

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

  public reviewList: UserReview[] = [];
  public totalReviews: number = 0;
  public formatDate = formatDate;
  private filters: reviewFilters = {};

  private report: Report = {
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
    private datePipe: DatePipe,
  ) {}

  ngAfterViewInit() {
    this.loadReviews();
    this.paginator.page.subscribe(() => this.loadReviews());
  }

  loadReviews() {
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
        if (response) {
          this.reviewList = response.content;
          this.totalReviews = response.totalElements;
        } else {
          this.reviewList = [];
          this.totalReviews = 0;
        }
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.userReviewService.getUserReviewsForGame(this.gameTitle, page, size, sortBy, sortDir, this.filters).subscribe(observer);
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
      const username = this.authService.getUsername();

      if (username === null) {
        console.log("Username is null");
        return;
      }

      review.userUsername = username;

      this.userReviewService.deleteUserReview(review).subscribe({
        next: () => {
          this.reviewList = this.reviewList.filter(r => r.id !== review.id);
          this.usersScoreUpdated.emit(review.score);
          this.notificationService.popSuccessToast('Deleted review successfuly');
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
    this.userReviewService.updateUserReviewRating(review).subscribe({
      error: error => console.error(error)
    });
  }

  sendReportInformation(review: UserReview, dialogRef: MatDialogRef<ReviewReportDialogComponent>) {
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

    this.reportService.reportReview(this.report).subscribe({
      next: () => { this.notificationService.popSuccessToast('Report sent successfully'); },
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

  // Filters

  onStartDateChange(event: MatDatepickerInputEvent<Date>) {
    const selectedDate = event.value;

    if (selectedDate) {
      const formattedDate = this.datePipe.transform(selectedDate, 'yyyy-MM-dd');
      
      if (formattedDate) {
        this.filters.startDate = formattedDate;
      }

      if (this.filters.startDate && this.filters.endDate) {
        this.loadReviews();
      }
    }
  }

  onEndDateChange(event: MatDatepickerInputEvent<Date>) {
    const selectedDate = event.value;

    if (selectedDate) {
      const formattedDate = this.datePipe.transform(selectedDate, 'yyyy-MM-dd');

      if (formattedDate) {
        this.filters.endDate = formattedDate;
      }

      if (this.filters.startDate && this.filters.endDate) {
        this.loadReviews();
      }
    }
  }

  onScoreMinFilterChange(value: number) {
    this.filters.scoreMin = value;
    if (!this.filters.scoreMax) {
      this.filters.scoreMax = 10;
    }
    this.loadReviews();
  }

  onScoreMaxFilterChange(value: number) {
    this.filters.scoreMax = value;
    if (!this.filters.scoreMin) {
      this.filters.scoreMin = 1;
    }
    this.loadReviews();
  }
}
