import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../../services/auth.service';
import { MatSort, Sort } from '@angular/material/sort';
import { formatDate } from '../../../util/formatDate';
import { Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { BackgroundService } from '../../../services/background.service';
import { CriticReview } from '../../../interfaces/criticReview';
import { CriticReviewService } from '../../../services/critic-review.service';
import { PopupDialogComponent } from '../../popup-dialog/popup-dialog.component';
import { Toast, ToasterService } from 'angular-toaster';
import { reviewStatuses } from '../../../interfaces/reviewStatuses';

@Component({
  selector: 'app-critic-reviews-list',
  templateUrl: './critic-reviews-list.component.html',
  styleUrl: './critic-reviews-list.component.css'
})
export class CriticReviewListComponent implements AfterViewInit {
  reviewsList: CriticReview[] = [];
  dataSource: MatTableDataSource<CriticReview> = new MatTableDataSource<CriticReview>([]);
  totalReviews: number = 0;
  displayedColumns: string[] = ['gameTitle', 'user', 'content', 'postDate', 'score', 'reviewStatus', 'options'];
  formatDate = formatDate;
  reviewStatuses = reviewStatuses;

  @ViewChild('paginator') paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private route: ActivatedRoute,
    private criticReviewService: CriticReviewService,
    private authService: AuthService,
    public dialog: MatDialog,
    private _location: Location,
    private backgroundService: BackgroundService,
    private toasterService: ToasterService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.backgroundService.setMainContentStyle({'padding-left': '200px'});
  }

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
          this.reviewsList = response.content;
          this.dataSource = new MatTableDataSource<CriticReview>(response.content)
        }
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.criticReviewService.getAllReviews(token, page, size, sortBy, sortDir).subscribe(observer);
  }

  sortData(sort: Sort) {
    this.loadReviews();
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

    const observer: Observer<any> = {
      next: response => {
        var toast: Toast = {
          type: 'success',
          title: 'Review approved',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'Approving failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {}
    };
    this.criticReviewService.reviewReview(review, token).subscribe(observer);
  }

  openDeleteReviewDialog(review: CriticReview) {
    const dialogTitle = 'Confirm review deletion';
    var dialogContent = 'Are you sure you want to delete this review?';
    const submitText = 'Delete';
    const cancelText = 'Cancel';

    if (review.user) {
      dialogContent = 'Are you sure you want to delete this review by ' + review.user.nickname + '?';
    }

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

  deleteReview(review: CriticReview) {
    const token = this.authService.getToken();

    if (token == null) {
      console.log("Token is null");
      return;
    }

    if (review.id == null) {
      console.log("Review is null");
      return;
    }

    review.reviewStatus = 'DELETED'

    const observer: Observer<any> = {
      next: response => {
        var toast: Toast = {
          type: 'success',
          title: 'Review deleted successfully',
          showCloseButton: true
        };
        this.toasterService.pop(toast);

        //this.reviewsList = this.reviewsList.filter(r => r.id !== review.id);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'Deletion submission failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {}
    };
    //this.criticReviewService.deleteReview(review.id, token).subscribe(observer);
    this.criticReviewService.reviewReview(review, token).subscribe(observer);
  }

  canDeleteReview(review: CriticReview) {
    if (this.authService.hasRole('Admin')) {
      return true;
    }

    return review.user && this.authService.hasRole('Critic') && review.user.username === this.authService.getUsername();
  }

  findReviewStatusName(review: CriticReview) {
    var status = reviewStatuses.find(rs => rs.className === review.reviewStatus)?.name;

    if (status == 'Approved' && review.approvedBy) {
      status += ' (by ' + review.approvedBy.nickname + ')';
    }

    return status;
  }

  editReview(review: CriticReview) {
    this.router.navigate(['/critic-reviews/edit/' + review.id]);
  }
}
