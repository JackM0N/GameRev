import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { Sort, MatSort } from '@angular/material/sort';
import { PopupDialogComponent } from '../../popup-dialog/popup-dialog.component';
import { UserGameService } from '../../../services/user-game.service';
import { AuthService } from '../../../services/auth.service';
import { UserGame } from '../../../interfaces/userGame';
import { UserGameEditDialogComponent } from '../user-game-edit-dialog/user-game-edit-dialog.component';
import { Toast, ToasterService } from 'angular-toaster';
import { UserGameAddDialogComponent } from '../user-game-add-dialog/user-game-add-dialog.component';

@Component({
  selector: 'app-user-games-list',
  templateUrl: './user-games-list.component.html'
})
export class UserGamesListComponent implements AfterViewInit {
  gamesList: UserGame[] = [];
  totalGames: number = 0;
  dataSource: MatTableDataSource<UserGame> = new MatTableDataSource<UserGame>(this.gamesList);
  displayedColumns: string[] = ['game', 'completionStatus', 'isFavourite', 'options'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private userGameService: UserGameService,
    private authService: AuthService,
    private toasterService: ToasterService,
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
    var nickname = this.authService.getNickname();

    if (!nickname) {
      console.log('Nickname is not valid.');
      return;
    }

    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = this.sort.active || 'id';
    const sortDir = this.sort.direction || 'asc';

    const observer: Observer<any> = {
      next: response => {
        this.gamesList = response.content;
        this.totalGames = response.totalElements;
        this.dataSource = new MatTableDataSource<UserGame>(this.gamesList);
        this.dataSource.data = this.gamesList;
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.userGameService.getUserGames(nickname, page, size, sortBy, sortDir).subscribe(observer);
  }

  openEditUserGameDialog(userGame: UserGame) {
    const dialogTitle = 'Updating game ' + userGame.game.title;

    const dialogRef = this.dialog.open(UserGameEditDialogComponent, {
      width: '300px',
      data: { dialogTitle, userGame }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true && dialogRef.componentRef) {
        const form = dialogRef.componentRef.instance.updateForm

        if (form) {
          userGame.completionStatus = form.get('completionStatus')?.value;
          userGame.isFavourite = form.get('isFavourite')?.value;
          this.editUserGame(userGame);

        } else {
          console.log("Form is null");
          return;
        }
      }
    });
  }

  editUserGame(userGame: UserGame) {
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        var toast: Toast = {
          type: 'success',
          title: 'Game updated successfully',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'Game updating failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {
      }
    };
    this.userGameService.updateUserGame(userGame, token).subscribe(observer);
  }

  openAddUserGameDialog() {
    const dialogTitle = 'Adding game';

    const dialogRef = this.dialog.open(UserGameAddDialogComponent, {
      width: '300px',
      data: { dialogTitle, existingGames: this.gamesList }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true && dialogRef.componentRef) {
        const form = dialogRef.componentRef.instance.addForm

        if (form) {
          var userGame: UserGame = {
            id: 0,
            user : { username: this.authService.getUsername() },
            game: form.get('game')?.value,
            completionStatus: form.get('completionStatus')?.value,
            isFavourite: form.get('isFavourite')?.value
          };

          userGame.completionStatus = form.get('completionStatus')?.value;
          userGame.isFavourite = form.get('isFavourite')?.value;
          this.addUserGame(userGame);

        } else {
          console.log("Form is null");
          return;
        }
      }
    });
  }

  addUserGame(userGame: UserGame) {
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        var toast: Toast = {
          type: 'success',
          title: 'Game added successfully',
          showCloseButton: true
        };
        this.toasterService.pop(toast);

        this.gamesList.push(userGame);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'Game adding failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {
      }
    };
    this.userGameService.addUserGame(userGame, token).subscribe(observer);
  }

  openGameDeletionConfirmationDialog(userGame: UserGame) {
    const dialogTitle = 'Game deletion';
    const dialogContent = 'Are you sure you want to delete the game ' + userGame.game.title + '?';
    const submitText = 'Delete';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteGame(userGame);
      }
    });
  }

  deleteGame(userGame: UserGame) {
    const token = this.authService.getToken();

    if (token === null) {
      console.log("Token is null");
      return;
    }

    if (!userGame || !userGame.id) {
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
    this.userGameService.deleteUserGame(userGame.id, token).subscribe(observer);
  }

  sortData(sort: Sort) {
    this.loadGames();
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }
}
