import { AfterViewInit, Component, ElementRef, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { ReportService } from '../../../services/report.service';
import { Report } from '../../../interfaces/report';
import { UserReview } from '../../../interfaces/userReview';
import { formatDateArray } from '../../../util/formatDate';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { NotificationService } from '../../../services/notification.service';
import { reviewFilters } from '../../../interfaces/reviewFilters';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { DatePipe } from '@angular/common';
import { FormBuilder, FormGroup } from '@angular/forms';

class ReportInformation {
  reports: Report[] = [];
  totalReports: number = 0;
  dataSource: MatTableDataSource<Report> = new MatTableDataSource<Report>([]);
}

@Component({
  selector: 'app-report-list',
  templateUrl: './report-list.component.html',
  styleUrl: './report-list.component.css'
})
export class ReportListComponent implements AfterViewInit {
  public reviewsList: UserReview[] = [];
  public totalReviews: number = 0;
  public noReviews = false;

  public reportsList: ReportInformation[] = [];
  public displayedColumns: string[] = ['id', 'user', 'content', 'options'];
  public formatDate = formatDateArray;
  
  private filters: reviewFilters = {};
  protected filterForm: FormGroup;

  @ViewChild('reviewsPaginator') reviewsPaginator!: MatPaginator;
  @ViewChildren(MatPaginator) paginators!: QueryList<MatPaginator>;
  @ViewChildren(MatPaginator, { read: ElementRef }) paginatorElements!: QueryList<ElementRef>;

  constructor(
    private reportService: ReportService,
    private notificationService: NotificationService,
    private fb: FormBuilder,
    public dialog: MatDialog,
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
    this.reviewsPaginator.page.subscribe(() => this.loadReviews());
  }

  loadReviews() {
    const page = this.reviewsPaginator.pageIndex + 1;
    const size = this.reviewsPaginator.pageSize;

    const observer: Observer<any> = {
      next: response => {
        if (response) {
          this.reviewsList = response.content;
          this.totalReviews = response.totalElements;
          this.reportsList = [];
          this.noReviews = (this.reviewsList.length == 0);
        } else {
          this.noReviews = true;
        }
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.reportService.getReviewsWithReports(page, size, "id", "asc", this.filters).subscribe(observer);
  }

  loadReportsForReview(review: UserReview, refreshing: boolean = false) {
    if (!refreshing && review.id != undefined && this.reportsList[review.id] !== undefined) {
      return;
    }

    if (!review.id) {
      console.log("Review id is null");
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
    this.reportService.getReportsForReview(review.id, sortBy, sortDir, page, size).subscribe(observer);
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  onPanelOpened(review: UserReview) {
    this.loadReportsForReview(review);
  }

  approveReport(report: Report) {
    this.reportService.approveReport(report).subscribe({
      next: () => { this.notificationService.popSuccessToast('Report approved'); },
      error: error => this.notificationService.popErrorToast('Report approving failed', error)
    });
  }

  disapproveReport(report: Report) {
    this.reportService.disapproveReport(report).subscribe({
      next: () => { this.notificationService.popSuccessToast('Report disapproved'); },
      error: error => this.notificationService.popErrorToast('Report disapproving failed', error)
    });
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
    this.reportService.deleteReview(review).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Report disapproved');
        this.reviewsList = this.reviewsList.filter(r => r.id !== review.id);
      },
      error: error => this.notificationService.popErrorToast('Report disapproving failed', error)
    });
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
