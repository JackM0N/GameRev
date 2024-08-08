import { AfterViewInit, ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { GameService } from '../../../services/game.service';
import { Game } from '../../../interfaces/game';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { Sort, MatSort } from '@angular/material/sort';
import { PopupDialogComponent } from '../../popup-dialog/popup-dialog.component';
import { AuthService } from '../../../services/auth.service';
import { releaseStatuses } from '../../../interfaces/releaseStatuses';
import { ReleaseStatus } from '../../../interfaces/releaseStatus';
import { BackgroundService } from '../../../services/background.service';

@Component({
  selector: 'app-games-list',
  templateUrl: './games-list.component.html'
})
export class GamesListComponent implements AfterViewInit {
  releaseStatuses: ReleaseStatus[] = releaseStatuses;
  gamesList: Game[] = [];
  totalGames: number = 0;
  dataSource: MatTableDataSource<Game> = new MatTableDataSource<Game>(this.gamesList);
  displayedColumns: string[] = ['id', 'title', 'developer', 'publisher', 'releaseDate', 'releaseStatus', 'usersScore', 'tags', 'description', 'options'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private gameService: GameService,
    private router: Router,
    public dialog: MatDialog,
    public authService: AuthService,
    private backgroundService: BackgroundService,
  ) {}

  ngOnInit(): void {
    this.backgroundService.setClasses(['fallingCds']);
    this.backgroundService.setMainContentStyle({'padding-left': '200px'});
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;

    this.loadGames();

    this.paginator.page.subscribe(() => this.loadGames());
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadGames();
    });
  }

  loadGames() {
    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = this.sort.active || 'id';
    const sortDir = this.sort.direction || 'asc';

    const observer: Observer<any> = {
      next: response => {
        this.gamesList = response.content;
        this.totalGames = response.totalElements;
        this.dataSource = new MatTableDataSource<Game>(this.gamesList);
        this.dataSource.data = this.gamesList;
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.gameService.getGames(page, size, sortBy, sortDir).subscribe(observer);
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
        this.gamesList = response;
        this.dataSource.data = this.gamesList;
        this.loadGames();
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };

    this.gameService.deleteGame(game.id).subscribe(observer);
  }

  getTags(game: Game) {
    return game.tags.map(tag => tag.tagName).join(', ');
  }

  sortData(sort: Sort) {
    this.loadGames();
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  findReleaseStatusName(status: string) {
    const releaseStatus = this.releaseStatuses.find(releaseStatus => releaseStatus.className === status);
    return releaseStatus ? releaseStatus.name : undefined;
  }
}
