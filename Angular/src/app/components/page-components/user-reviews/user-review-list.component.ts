import { AfterViewInit, ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Observer } from 'rxjs';
import { UserReview } from '../../../models/userReview';
import { UserReviewService } from '../../../services/user-review.service';
import { MatSort } from '@angular/material/sort';
import { formatDateArray } from '../../../util/formatDate';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { BackgroundService } from '../../../services/background.service';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { UserReviewFormDialogComponent } from './user-review-form-dialog.component';
import { AuthService } from '../../../services/auth.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-user-review-list',
  templateUrl: './user-review-list.component.html'
})
export class UserReviewListComponent extends BaseAdComponent implements AfterViewInit {
  protected reviewList: UserReview[] = [];
  protected dataSource: MatTableDataSource<UserReview> = new MatTableDataSource<UserReview>([]);
  protected totalReviews: number = 0;
  protected displayedColumns: string[] = ['gameTitle', 'content', 'postDate', 'score', 'options'];
  protected userId?: number;
  protected formatDate = formatDateArray;

  @ViewChild('paginator') protected paginator!: MatPaginator;
  @ViewChild(MatSort) protected sort!: MatSort;

  constructor(
    private route: ActivatedRoute,
    private userReviewService: UserReviewService,
    private _location: Location,
    private backgroundService: BackgroundService,
    private authService: AuthService,
    private notificationService: NotificationService,
    protected dialog: MatDialog,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
  }

  override ngOnInit(): void {
    super.ngOnInit();
    
    this.backgroundService.setClasses(['fallingCds']);
  }

  ngAfterViewInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.userId = params['id'];
      }
      this.loadReviews();
    });

    this.paginator.page.subscribe(() => this.loadReviews());
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
          this.reviewList = response.content;
          this.dataSource = new MatTableDataSource<UserReview>(response.content);
        }
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    if (this.userId) {
      this.userReviewService.getUserReviewsAdmin(this.userId, page, size, sortBy, sortDir).subscribe(observer);
    } else {
      this.userReviewService.getOwnUserReviews(page, size, sortBy, sortDir).subscribe(observer);
    }
  }

  sortData() {
    this.loadReviews();
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  goBack() {
    this._location.back();
  }

  openEditReviewDialog(review: UserReview) {
    const dialogRef = this.dialog.open(UserReviewFormDialogComponent, {
      width: '400px',
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

  openReviewDeletionConfirmationDialog(review: UserReview) {
    const dialogTitle = 'Confirm review deletion';
    const dialogContent = 'Are you sure you want to delete this review?';
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
          this.dataSource = new MatTableDataSource<UserReview>(this.reviewList);
          this.notificationService.popSuccessToast('Deleted review successfuly');
        },
        error: error => this.notificationService.popErrorToast('Deleting review failed', error)
      });
    }
  }
}
