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
import { AuthService } from '../../../services/auth.service';
import { NotificationService } from '../../../services/notification.service';
import { ReportService } from '../../../services/report.service';
import { Report } from '../../../models/report';

@Component({
  selector: 'app-report-review-list',
  templateUrl: './user-report-list.component.html'
})
export class UserReportListComponent extends BaseAdComponent implements AfterViewInit {
  protected reportList: UserReview[] = [];
  protected dataSource: MatTableDataSource<UserReview> = new MatTableDataSource<UserReview>([]);
  protected totalReports: number = 0;
  protected displayedColumns: string[] = ['content', 'approved', 'options'];
  protected userId?: number;
  protected formatDate = formatDateArray;

  @ViewChild('paginator') protected paginator!: MatPaginator;
  @ViewChild(MatSort) protected sort!: MatSort;

  constructor(
    private route: ActivatedRoute,
    private reportService: ReportService,
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
      this.loadReports();
    });

    this.paginator.page.subscribe(() => this.loadReports());
  }

  loadReports() {
    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = this.sort.active || 'id';
    const sortDir = this.sort.direction || 'asc';

    this.reportService.getOwnUserReports(sortBy, sortDir, page, size).subscribe({
      next: (response: any) => {
        console.log(response);
        if (response) {
          this.totalReports = response.totalElements;
          this.reportList = response.content;
          this.dataSource = new MatTableDataSource<UserReview>(response.content);
        }
      },
      error: error => { console.error(error); }
    });
  }

  openReportDeletionConfirmationDialog(report: Report) {
    const dialogTitle = 'Confirm review deletion';
    const dialogContent = 'Are you sure you want to delete this report?';
    const submitText = 'Delete';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteReport(report);
      }
    });
  }

  deleteReport(report: Report) {
    this.reportService.deleteReport(report).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Report deleted successfully');
        this.reportList = this.reportList.filter(r => r.id !== report.id);
        this.dataSource = new MatTableDataSource<UserReview>(this.reportList);
        this.totalReports--;
      },
      error: error => this.notificationService.popErrorToast('Report deleting failed', error)
    });
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  goBack() {
    this._location.back();
  }
}
