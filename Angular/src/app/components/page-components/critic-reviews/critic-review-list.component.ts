
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { debounceTime, distinctUntilChanged, fromEvent, map, Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../../services/auth.service';
import { MatSort } from '@angular/material/sort';
import { formatDateArray } from '../../../util/formatDate';
import { CriticReview } from '../../../interfaces/criticReview';
import { CriticReviewService } from '../../../services/critic-review.service';
import { reviewStatuses } from '../../../enums/reviewStatuses';
import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe, Location } from '@angular/common';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { NotificationService } from '../../../services/notification.service';
import { MatSelectChange } from '@angular/material/select';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { criticReviewFilters } from '../../../interfaces/criticReviewFilters';

@Component({
  selector: 'app-critic-review-list',
  templateUrl: './critic-review-list.component.html',
  styleUrl: './critic-review-list.component.css'
})
export class CriticReviewListComponent implements AfterViewInit {
  public dataSource: MatTableDataSource<CriticReview> = new MatTableDataSource<CriticReview>([]);
  public totalReviews: number = 0;
  public displayedColumns: string[] = ['gameTitle', 'user', 'content', 'postDate', 'score', 'reviewStatus', 'options'];
  public formatDate = formatDateArray;
  public reviewStatuses = reviewStatuses;
  public noCriticReviews = false;

  private filters: criticReviewFilters = {};

  @ViewChild('paginator') paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild('searchInput', { static: true }) searchInput?: ElementRef;

  constructor(
    private criticReviewService: CriticReviewService,
    private authService: AuthService,
    private dialog: MatDialog,
    private _location: Location,
    private notificationService: NotificationService,
    private router: Router,
    private datePipe: DatePipe,
  ) {}

  ngAfterViewInit() {
    this.loadReviews();
    this.paginator.page.subscribe(() => this.loadReviews());

    if (this.searchInput) {
      fromEvent(this.searchInput.nativeElement, 'input').pipe(
        map((event: any) => event.target.value),
        debounceTime(300),
        distinctUntilChanged()

      ).subscribe(value => {
        this.onSearchChange(value);
      });
    }
  }

  loadReviews() {
    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = this.sort.active || 'id';
    const sortDir = this.sort.direction || 'asc';

    const observer: Observer<any> = {
      next: response => {
        if (response) {
          this.totalReviews = response.totalElements;
          this.dataSource = new MatTableDataSource<CriticReview>(response.content);
          this.noCriticReviews = (this.dataSource.data.length == 0);

        } else {
          this.noCriticReviews = true
        }
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.criticReviewService.getAllReviews(page, size, sortBy, sortDir, this.filters).subscribe(observer);
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  goBack() {
    this._location.back();
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

  deleteReview(review: CriticReview) {
    if (review.id == null) {
      console.log("Review is null");
      return;
    }

    this.criticReviewService.deleteReview(review.id).subscribe({
      next: () => { this.notificationService.popSuccessToast('Review deleted successfully'); },
      error: error => this.notificationService.popErrorToast('Review deletion failed', error)
    });
  }

  canDeleteReview(review: CriticReview) {
    if (this.authService.hasRole('Admin')) {
      return true;
    }

    return review.user && this.authService.hasRole('Critic') && review.user.username === this.authService.getUsername();
  }

  findReviewStatusName(review: CriticReview) {
    var status = reviewStatuses.find(rs => rs.className === review.reviewStatus)?.name;

    if (review.statusChangedBy) {
      status += ' (by ' + review.statusChangedBy.nickname + ')';
    }

    return status;
  }

  editReview(review: CriticReview) {
    this.router.navigate(['/critic-reviews/edit/' + review.id]);
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
}
