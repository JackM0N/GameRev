import { AfterViewInit, Component, ElementRef, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { ReportService } from '../../../services/report.service';
import { Report } from '../../../interfaces/report';
import { AuthService } from '../../../services/auth.service';
import { UserReview } from '../../../interfaces/userReview';
import { UserReviewService } from '../../../services/user-review.service';
import { formatDate } from '../../../util/formatDate';
import { Toast, ToasterService } from 'angular-toaster';
import { PopupDialogComponent } from '../../popup-dialog/popup-dialog.component';

class ReportInformation {
  reports: Report[] = [];
  totalReports: number = 0;
  dataSource: MatTableDataSource<Report> = new MatTableDataSource<Report>([]);
}

@Component({
  selector: 'app-reports-list',
  templateUrl: './reports-list.component.html',
  styleUrl: './reports-list.component.css'
})
export class ReportsListComponent implements AfterViewInit {
  reviewsList: UserReview[] = [];
  totalReviews: number = 0;

  reportsList: ReportInformation[] = [];
  displayedColumns: string[] = ['id', 'user', 'content', 'options'];
  formatDate = formatDate;

  @ViewChild('reviewsPaginator') reviewsPaginator!: MatPaginator;
  @ViewChildren(MatPaginator) paginators!: QueryList<MatPaginator>;
  @ViewChildren(MatPaginator, { read: ElementRef }) paginatorElements!: QueryList<ElementRef>;

  constructor(
    private reportService: ReportService,
    private userReviewService: UserReviewService,
    private authService: AuthService,
    public dialog: MatDialog,
    private toasterService: ToasterService,
  ) {}

  ngAfterViewInit() {
    this.loadReviews();

    this.reviewsPaginator.page.subscribe(() => this.loadReviews());
  }

  loadReviews() {
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    const page = this.reviewsPaginator.pageIndex + 1;
    const size = this.reviewsPaginator.pageSize;

    const observer: Observer<any> = {
      next: response => {
        if (response) {
          this.reviewsList = response.content;
          this.totalReviews = response.totalElements;
          this.reportsList = [];
        }
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.userReviewService.getReviewsWithReports(token, page, size, "id", "asc").subscribe(observer);
  }

  loadReportsForReview(review: UserReview, refreshing: boolean = false) {
    if (!refreshing && review.id != undefined && this.reportsList[review.id] !== undefined) {
      return;
    }

    const token = this.authService.getToken();

    if (!review.id) {
      console.log("Review id is null");
      return;
    }

    if (token === null) {
      console.log("Token is null");
      return;
    }

    var page = undefined;
    var size = undefined;
    const sortBy = 'id';
    const sortDir = 'asc';

    this.paginators.forEach((paginator, index) => {
      const paginatorElement = this.paginatorElements.toArray()[index];

      if (paginatorElement.nativeElement.id == review.id) {
        page = paginator.pageIndex + 1;
        size = paginator.pageSize;
      }
    });

    const observer: Observer<any> = {
      next: response => {
        if (review.id) {
          if (response) {
            this.reportsList[review.id] = {
              reports: response.content,
              totalReports: response.totalElements,
              dataSource: new MatTableDataSource<Report>(response.content)
            };
            
            if (!refreshing) {
              setTimeout(() => {
                this.paginators.forEach((paginator, index) => {
                  const paginatorElement = this.paginatorElements.toArray()[index];
    
                  if (paginatorElement.nativeElement.id == review.id) {
                    if (review.id) {
                      this.reportsList[review.id].dataSource.paginator = paginator;
                    }
                    paginator.page.subscribe(() => this.loadReportsForReview(review, true));

                  }
                });
              });
            }
          } else {
            this.reportsList[review.id] = {
              reports: [],
              totalReports: 0,
              dataSource: new MatTableDataSource<Report>([])
            };
          }
        }
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.reportService.getReportsForReview(review.id, token, sortBy, sortDir, page, size).subscribe(observer);
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  onPanelOpened(review: UserReview) {
    this.loadReportsForReview(review);
  }

  approveReport(report: Report) {
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        var toast: Toast = {
          type: 'success',
          title: 'Report approved',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'Report approving failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {}
    };
    this.reportService.approveReport(report, token).subscribe(observer);
  }

  disapproveReport(report: Report) {
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        var toast: Toast = {
          type: 'success',
          title: 'Report disapproved',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'Report disapproving failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {}
    };
    this.reportService.disapproveReport(report, token).subscribe(observer);
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
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        var toast: Toast = {
          type: 'success',
          title: 'Review deleted',
          showCloseButton: true
        };
        this.toasterService.pop(toast);

        this.reviewsList = this.reviewsList.filter(r => r.id !== review.id);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'Review deletion failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {}
    };
    this.reportService.deleteReview(review, token).subscribe(observer);
  }
}
