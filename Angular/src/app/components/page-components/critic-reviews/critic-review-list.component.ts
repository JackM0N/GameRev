
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { debounceTime, distinctUntilChanged, fromEvent, map } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../../services/auth.service';
import { MatSort } from '@angular/material/sort';
import { formatDateArray } from '../../../util/formatDate';
import { CriticReview } from '../../../models/criticReview';
import { CriticReviewService } from '../../../services/critic-review.service';
import { reviewStatuses } from '../../../enums/reviewStatuses';
import { AfterViewInit, Component, ElementRef, ViewChild, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { NotificationService } from '../../../services/notification.service';
import { MatSelectChange } from '@angular/material/select';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { criticReviewFilters } from '../../../filters/criticReviewFilters';
import { FormBuilder, FormGroup } from '@angular/forms';
import { BackgroundService } from '../../../services/background.service';
import { AdService } from '../../../services/ad.service';
import { CriticReviewContentDialogComponent } from './critic-review-content-dialog.component';
import { CriticReviewFormDialogComponent } from './critic-review-form-dialog.component';

@Component({
  selector: 'app-critic-review-list',
  templateUrl: './critic-review-list.component.html'
})
export class CriticReviewListComponent implements AfterViewInit, OnInit {
  protected dataSource: MatTableDataSource<CriticReview> = new MatTableDataSource<CriticReview>([]);
  protected totalReviews = 0;
  protected displayedColumns: string[] = ['gameTitle', 'user', 'content', 'postDate', 'score', 'reviewStatus', 'options'];
  protected formatDate = formatDateArray;
  protected reviewStatuses = reviewStatuses;

  private filters: criticReviewFilters = {};
  protected filterForm: FormGroup;

  @ViewChild('paginator') protected paginator!: MatPaginator;
  @ViewChild(MatSort) protected sort!: MatSort;
  @ViewChild('searchInput', { static: true }) protected searchInput?: ElementRef;

  constructor(
    private criticReviewService: CriticReviewService,
    private authService: AuthService,
    private dialog: MatDialog,
    private notificationService: NotificationService,
    private backgroundService: BackgroundService,
    private fb: FormBuilder,
    private datePipe: DatePipe,
    private adService: AdService
  ) {
    this.filterForm = this.fb.group({
      reviewStatuses: [null],
      dateRange: this.fb.group({
        start: [null],
        end: [null]
      }),
      userScore: this.fb.group({
        min: [null],
        max: [null]
      }),
      search: [null]
    });
  }

  ngOnInit(): void {
    this.adService.setAdVisible(false);
    this.backgroundService.setClasses(['fallingCds']);
  }

  ngAfterViewInit() {
    this.loadReviews();
    this.paginator.page.subscribe(() => this.loadReviews());

    if (this.searchInput) {
      fromEvent<InputEvent>(this.searchInput.nativeElement, 'input').pipe(
        map((event) => (event.target as HTMLInputElement).value),
        debounceTime(300),
        distinctUntilChanged()
        
      ).subscribe(value => {
        this.onSearchChange(value);
      });
    }
  }

  loadReviews() {
    this.totalReviews = 0;
    this.dataSource = new MatTableDataSource<CriticReview>([]);

    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = this.sort.active || 'id';
    const sortDir = this.sort.direction || 'asc';

    this.criticReviewService.getAllReviews(page, size, sortBy, sortDir, this.filters).subscribe({
      next: response => {
        if (response) {
          this.totalReviews = response.totalElements;
          this.dataSource = new MatTableDataSource<CriticReview>(response.content);
        }
      },
      error: error => { console.error(error); }
    });
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  approveReview(review: CriticReview) {
    review.reviewStatus = 'APPROVED'

    this.criticReviewService.reviewReview(review).subscribe({
      next: () => { this.notificationService.popSuccessToast('Review approved'); },
      error: error => this.notificationService.popErrorToast('Approving failed', error)
    });
  }

  openDeleteReviewDialog(review: CriticReview) {
    const dialogTitle = 'Confirm review deletion';
    const submitText = 'Delete';
    const cancelText = 'Cancel';

    const dialogContent = review.user
      ? `Are you sure you want to delete this review by ${review.user.nickname}?`
      : 'Are you sure you want to delete this review?';

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

  softDeleteReview(review: CriticReview) {
    review.reviewStatus = 'DELETED'

    this.criticReviewService.reviewReview(review).subscribe({
      next: () => { this.notificationService.popSuccessToast('Review deleted successfully'); },
      error: error => this.notificationService.popErrorToast('Review deletion failed', error)
    });
  }

  restoreReview(review: CriticReview) {
    review.reviewStatus = 'RESTORED'

    this.criticReviewService.reviewReview(review).subscribe({
      next: () => { this.notificationService.popSuccessToast('Review restored successfully'); },
      error: error => this.notificationService.popErrorToast('Review restoration failed', error)
    });
  }

  deleteReview(review: CriticReview) {
    if (review.id == null) {
      console.log("Review is null");
      return;
    }

    this.criticReviewService.deleteReview(review.id).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Review deleted successfully');
        if (this.dataSource.data.length === 1 && this.paginator.pageIndex > 0) {
          this.paginator.previousPage();
        }
        this.loadReviews();
      },
      error: error => this.notificationService.popErrorToast('Review deletion failed', error)
    });
  }

  canDeleteReview(review: CriticReview) {
    if (this.authService.hasRole('Admin')) {
      return true;
    }

    return review.user && this.authService.hasRole('Critic') && review.user.nickname === this.authService.getNickname();
  }

  findReviewStatusName(review: CriticReview) {
    const status = reviewStatuses.find(rs => rs.className === review.reviewStatus)?.name;
    return status;
  }

  openEditCriticReviewDialog(review: CriticReview) {
    const dialogRef = this.dialog.open(CriticReviewFormDialogComponent, {
      data: {
        editing: true,
        review: review
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.loadReviews();
      }
    });
  }

  // Filters

  onReviewStatusesFilterChange(event: MatSelectChange) {
    this.filters.reviewStatus = event.value;
    this.loadReviews();
  }

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

  onSearchChange(value: string) {
    this.filters.search = value;
    this.loadReviews();
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

  openContentDialog(review: CriticReview) {
    const dialogTitle = 'Review by ' + review.user?.nickname;
    const dialogContent = review.content;

    this.dialog.open(CriticReviewContentDialogComponent, {
      data: { dialogTitle, dialogContent }
    });
  }
}
