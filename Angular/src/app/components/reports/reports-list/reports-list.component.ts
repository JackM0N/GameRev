import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { Game } from '../../../interfaces/game';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { Sort, MatSort } from '@angular/material/sort';
import { PopupDialogComponent } from '../../popup-dialog/popup-dialog.component';
import { ReportService } from '../../../services/report.service';
import { Report } from '../../../interfaces/report';
import { AuthService } from '../../../services/auth.service';
import { UserReview } from '../../../interfaces/userReview';
import { UserReviewService } from '../../../services/user-review.service';

@Component({
  selector: 'app-reports-list',
  templateUrl: './reports-list.component.html'
})
export class ReportsListComponent implements AfterViewInit {
  reviewsList: Report[] = [];
  totalReviews: number = 0;
  dataSource: MatTableDataSource<UserReview> = new MatTableDataSource<UserReview>(this.reviewsList);
  displayedColumns: string[] = ['id', 'gameTitle', 'userUsername', 'content', 'postDate', 'score', 'positiveRating', 'negativeRating', 'options'];
  //displayedColumns: string[] = ['id', 'content', 'userReview', 'userId', 'approved', 'options'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private reportService: ReportService,
    private userReviewService: UserReviewService,
    private authService: AuthService,
    private router: Router,
    public dialog: MatDialog,
  ) {}

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;

    this.loadReports();

    this.paginator.page.subscribe(() => this.loadReports());
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadReports();
    });
  }

  loadReports() {
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
        console.log("response");
        console.log(response);

        this.reviewsList = response.content;
        this.totalReviews = response.totalElements;
        this.dataSource = new MatTableDataSource<UserReview>(this.reviewsList);
        this.dataSource.data = this.reviewsList;
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.userReviewService.getReviewsWithReports(token, page, size, sortBy, sortDir).subscribe(observer);
  }

  routeToAddNewGame() {
    this.router.navigate(['/games/add']);
  }

  routeToEditGame(title: string) {
    this.router.navigate(['/games/edit/' + title]);
  }

  routeToViewGame(title: string) {
    this.router.navigate(['/game/' + title]);
  }

  openGameDeletionConfirmationDialog(game: Game) {
    const dialogTitle = 'Game deletion';
    const dialogContent = 'Are you sure you want to delete the game ' + game.title + '?';
    const submitText = 'Delete';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteGame(game);
      }
    });
  }

  deleteGame(game: Game) {
    if (!game || !game.id) {
      console.log('Game ID is not valid.');
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        this.reviewsList = response;
        this.dataSource.data = this.reviewsList;
        this.loadReports();
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };

    //this.gameService.deleteGame(game.id).subscribe(observer);
  }

  sortData(sort: Sort) {
    this.loadReports();
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }
}
