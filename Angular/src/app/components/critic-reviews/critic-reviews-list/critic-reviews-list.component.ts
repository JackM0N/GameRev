import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../../services/auth.service';
import { MatSort, Sort } from '@angular/material/sort';
import { formatDate } from '../../../util/formatDate';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { BackgroundService } from '../../../services/background.service';
import { CriticReview } from '../../../interfaces/criticReview';
import { CriticReviewService } from '../../../services/critic-review.service';

@Component({
  selector: 'app-critic-reviews-list',
  templateUrl: './critic-reviews-list.component.html',
  styleUrl: './critic-reviews-list.component.css'
})
export class CriticReviewListComponent implements AfterViewInit {
  reviewsList: CriticReview[] = [];
  dataSource: MatTableDataSource<CriticReview> = new MatTableDataSource<CriticReview>([]);
  totalReviews: number = 0;
  displayedColumns: string[] = ['gameTitle', 'content', 'postDate', 'score', 'options'];
  formatDate = formatDate;

  @ViewChild('paginator') paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private route: ActivatedRoute,
    private criticReviewService: CriticReviewService,
    private authService: AuthService,
    public dialog: MatDialog,
    private _location: Location,
    private backgroundService: BackgroundService
  ) {}

  ngOnInit(): void {
    this.backgroundService.setMainContentStyle({'padding-left': '200px'});
    this.loadReviews();
  }

  ngAfterViewInit() {
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
    
  }

  disapproveReview(review: CriticReview) {
    
  }
}
