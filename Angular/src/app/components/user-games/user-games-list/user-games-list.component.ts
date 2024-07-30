import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { Game } from '../../../interfaces/game';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { Sort, MatSort } from '@angular/material/sort';
import { PopupDialogComponent } from '../../popup-dialog/popup-dialog.component';
import { UserGameService } from '../../../services/user-game.service';

@Component({
  selector: 'app-user-games-list',
  templateUrl: './user-games-list.component.html'
})
export class UserGamesListComponent implements AfterViewInit {
  gamesList: Game[] = [];
  totalGames: number = 0;
  dataSource: MatTableDataSource<Game> = new MatTableDataSource<Game>(this.gamesList);
  displayedColumns: string[] = ['id', 'title', 'developer', 'publisher', 'releaseDate', 'releaseStatus', 'usersScore', 'tags', 'description', 'options'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private userGameService: UserGameService,
    private router: Router,
    public dialog: MatDialog,
  ) {}

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
    const nickname = "";

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
    this.userGameService.getUserGames(nickname, page, size, sortBy, sortDir).subscribe(observer);
  }

  routeToAddNewGame() {
    this.router.navigate(['/library/add']);
  }

  routeToEditGame(title: string) {
    this.router.navigate(['/library/edit/' + title]);
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

    //this.userGameService.deleteGame(game.id).subscribe(observer);
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
}
