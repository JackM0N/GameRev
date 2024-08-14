
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../../services/auth.service';
import { MatSort } from '@angular/material/sort';
import { formatDate } from '../../../util/formatDate';
import { CriticReview } from '../../../interfaces/criticReview';
import { CriticReviewService } from '../../../services/critic-review.service';
import { reviewStatuses } from '../../../interfaces/reviewStatuses';
import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-critic-review-list',
  templateUrl: './critic-review-list.component.html',
  styleUrl: './critic-review-list.component.css'
})
export class CriticReviewListComponent implements AfterViewInit {
  public dataSource: MatTableDataSource<CriticReview> = new MatTableDataSource<CriticReview>([]);
  public totalReviews: number = 0;
  public displayedColumns: string[] = ['gameTitle', 'user', 'content', 'postDate', 'score', 'reviewStatus', 'options'];
  public formatDate = formatDate;
  public noCriticReviews = false;

  @ViewChild('paginator') paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private criticReviewService: CriticReviewService,
    private authService: AuthService,
    private dialog: MatDialog,
    private _location: Location,
    private notificationService: NotificationService,
    private router: Router,
  ) {}

  ngAfterViewInit() {
    this.loadReviews();
    this.paginator.page.subscribe(() => this.loadReviews());
  }

  loadReviews() {
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = this.sort.active || 'id';
    const sortDir = this.sort.direction || 'asc';

    const observer: Observer<any> = {
      next: response => {
        if (response) {
          this.totalReviews = response.totalElements;
          this.dataSource = new MatTableDataSource<CriticReview>(response.content);

          if (this.dataSource.data.length == 0) {
            this.noCriticReviews = true;
          }
        }
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.criticReviewService.getAllReviews(token, page, size, sortBy, sortDir).subscribe(observer);
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  goBack() {
    this._location.back();
  }

  approveReview(review: CriticReview) {
    const token = this.authService.getToken();

    if (token == null) {
      console.log("Token is null");
      return;
    }

    review.reviewStatus = 'APPROVED'

    this.criticReviewService.reviewReview(review, token).subscribe({
      next: () => { this.notificationService.popSuccessToast('Review approved', false); },
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
    const token = this.authService.getToken();

    if (token == null || review.id == null) {
      console.log("Token or review is null");
      return;
    }

    review.reviewStatus = 'DELETED'

    this.criticReviewService.reviewReview(review, token).subscribe({
      next: () => { this.notificationService.popSuccessToast('Review deleted successfully', false); },
      error: error => this.notificationService.popErrorToast('Review deletion failed', error)
    });
  }

  deleteReview(review: CriticReview) {
    const token = this.authService.getToken();

    if (token == null || review.id == null) {
      console.log("Token or review is null");
      return;
    }

    this.criticReviewService.deleteReview(review.id, token).subscribe({
      next: () => { this.notificationService.popSuccessToast('Review deleted successfully', false); },
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
}
