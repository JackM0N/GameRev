import { AfterViewInit, ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Observer } from 'rxjs';
import { UserReview } from '../../../interfaces/userReview';
import { UserReviewService } from '../../../services/user-review.service';
import { MatSort } from '@angular/material/sort';
import { formatDateArray } from '../../../util/formatDate';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { BackgroundService } from '../../../services/background.service';

@Component({
  selector: 'app-user-review-list',
  templateUrl: './user-review-list.component.html',
  styleUrl: './user-review-list.component.css'
})
export class UserReviewListComponent extends BaseAdComponent implements AfterViewInit {
  reviewsList: UserReview[] = [];
  dataSource: MatTableDataSource<UserReview> = new MatTableDataSource<UserReview>([]);
  totalReviews: number = 0;
  displayedColumns: string[] = ['gameTitle', 'content', 'postDate', 'score', 'options'];
  userId?: number;
  formatDate = formatDateArray;

  @ViewChild('paginator') paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private route: ActivatedRoute,
    private userReviewService: UserReviewService,
    private _location: Location,
    private backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
  }

  override ngOnInit(): void {
    this.backgroundService.setClasses(['fallingCds']);
  }

  override ngAfterViewInit() {
    super.ngAfterViewInit();

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
          this.reviewsList = response.content;
          this.dataSource = new MatTableDataSource<UserReview>(response.content)
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

  openEditReviewDialog(review: UserReview) {}

  openDeleteReviewDialog(review: UserReview) {}
}
