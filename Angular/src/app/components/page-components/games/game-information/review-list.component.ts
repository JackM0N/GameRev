import { AfterViewInit, Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { UserReviewService } from '../../../../services/user-review.service';
import { AuthService } from '../../../../services/auth.service';
import { ReportService } from '../../../../services/report.service';
import { UserReview } from '../../../../interfaces/userReview';
import { Observer } from 'rxjs';
import { ReviewReportDialogComponent } from '../review-report-dialog.component';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { PopupDialogComponent } from '../../../general-components/popup-dialog.component';
import { Game } from '../../../../interfaces/game';
import { Report } from '../../../../interfaces/report';
import { formatDateArray } from '../../../../util/formatDate';
import { NotificationService } from '../../../../services/notification.service';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { DatePipe } from '@angular/common';
import { reviewFilters } from '../../../../interfaces/reviewFilters';
import { FormBuilder, FormGroup } from '@angular/forms';
import { UserReviewFormDialogComponent } from '../../user-reviews/user-review-form-dialog.component';

@Component({
  selector: 'app-gameinfo-review-list',
  templateUrl: './review-list.component.html'
})
export class GameInfoReviewListComponent implements AfterViewInit {
  @Output() public usersScoreUpdated = new EventEmitter<number>();

  @Input() public gameTitle?: string;
  @Input() public game?: Game;

  @ViewChild(MatPaginator) protected paginator!: MatPaginator;
  @ViewChild(MatSort) protected sort!: MatSort;

  protected reviewList: UserReview[] = [];
  protected totalReviews: number = 0;
  protected formatDateArray = formatDateArray;
  
  private filters: reviewFilters = {};
  protected filterForm: FormGroup;

  private report: Report = {
    content: '',
    userReview: {
      id: -1
    }
  }

  constructor(
    private userReviewService: UserReviewService,
    private notificationService: NotificationService,
    protected authService: AuthService,
    private reportService: ReportService,
    private fb: FormBuilder,
    protected dialog: MatDialog,
    private datePipe: DatePipe,
  ) {
    this.filterForm = this.fb.group({
      dateRange: this.fb.group({
        start: [null],
        end: [null]
      }),
      userScore: this.fb.group({
        min: [null],
        max: [null]
      })
    });
  }

  ngAfterViewInit() {
    this.loadReviews();
    this.paginator.page.subscribe(() => this.loadReviews());
  }

  loadReviews() {
    if (this.gameTitle === undefined) {
      console.log("Game title is undefined");
      return;
    }

    this.reviewList = [];
    this.totalReviews = 0;

    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = 'id';
    const sortDir = 'asc';

    const observer: Observer<any> = {
      next: response => {
        if (response) {
          this.reviewList = response.content;
          this.totalReviews = response.totalElements;
        }
      },
      error: error => { console.error(error); },
      complete: () => {}
    };
    this.userReviewService.getUserReviewsForGame(this.gameTitle, page, size, sortBy, sortDir, this.filters).subscribe(observer);
  }

  openReviewDeletionConfirmationDialog(review: UserReview) {
    const dialogTitle = 'Confirm review deletion';
    const dialogContent = 'Are you sure you want to delete review by ' + review.userNickname + '?';
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
      review.userNickname = username;

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
    // If the user has already liked the review, remove the like
    if (review.ownRatingIsPositive == true) {
      review.ownRatingIsPositive = null;
      if (review.positiveRating != null) {
        review.positiveRating--;
      }

    } else {
      // If the user has already disliked the review, remove the dislike
      if (review.ownRatingIsPositive == false) {
        if (review.negativeRating != null) {
          review.negativeRating--;
        }
      }

      // Add a like to the review
      review.ownRatingIsPositive = true;
      if (review.positiveRating != null) {
        review.positiveRating++;
      }
    }

    this.sendRatingInformation(review);
  }

  toggleDislike(review: UserReview) {
    // If the user has already disliked the review, remove the dislike
    if (review.ownRatingIsPositive == false) {
      review.ownRatingIsPositive = null;
      if (review.negativeRating != null) {
        review.negativeRating--;
      }

    } else {
      // If the user has already liked the review, remove the like
      if (review.ownRatingIsPositive == true) {
        if (review.positiveRating != null) {
          review.positiveRating--;
        }
      }

      // Add a dislike to the review
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

  openReportReviewDialog(review: UserReview) {
    const dialogContent = 'Report review by ' + review.userNickname;

    const dialogRef = this.dialog.open(ReviewReportDialogComponent, {
      width: '400px',
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
    (this.authService.getUsername() === review.userNickname || this.authService.hasAnyRole(['Admin', 'Critic']));
  }

  ownsReview(review: UserReview) {
    return this.authService.isAuthenticated() && this.authService.getUsername() === review.userNickname;
  }

  canAddReview() {
    if (!this.authService.isAuthenticated()) {
      return false;
    }

    const userName = this.authService.getUsername();
    return !this.reviewList.some(review => review.userNickname === userName);
  }

  openAddReviewDialog() {
    if (this.game) {
      const dialogRef = this.dialog.open(UserReviewFormDialogComponent, {
        width: '400px',
        data: {
          editing: false,
          game: this.game
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result === true) {
          this.loadReviews();
        }
      });
    }
  }

  openEditReviewDialog(review: UserReview) {
    if (this.game) {
      const dialogRef = this.dialog.open(UserReviewFormDialogComponent, {
        width: '400px',
        data: {
          editing: true,
          game: this.game,
          review: review
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result === true) {
          this.loadReviews();
        }
      });
    }
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

  clearFilters() {
    this.filters = {};
    this.filterForm.reset();
    this.filterForm.get('userScore')?.get('min')?.setValue(1);
    this.filterForm.get('userScore')?.get('max')?.setValue(10);
    this.loadReviews();
  }
}
