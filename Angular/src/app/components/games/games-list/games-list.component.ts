import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { GameService } from '../../../services/game.service';
import { Game } from '../../../interfaces/game';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { GameDeletionConfirmationDialogComponent } from '../game-deletion-confirmation-dialog/game-deletion-confirmation-dialog.component';
import { Sort } from '@angular/material/sort';

@Component({
  selector: 'app-games-list',
  templateUrl: './games-list.component.html',
  styleUrl: '/src/app/styles/shared-list-styles.css'
})
export class ViewingGamesComponent implements AfterViewInit, OnInit {
  gamesList: Game[] = [];
  sortedData: Game[] = [];
  dataSource: MatTableDataSource<Game> = new MatTableDataSource<Game>(this.gamesList);
  displayedColumns: string[] = ['id', 'title', 'developer', 'publisher', 'releaseDate', 'releaseStatus', 'usersScore', 'tags', 'description', 'options'];
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private gameService: GameService,
    private router: Router,
    public dialog: MatDialog,
  ) {
  }

  ngOnInit() {
    const observer: Observer<any> = {
      next: response => {
        this.gamesList = response;
        this.dataSource = new MatTableDataSource<Game>(this.gamesList);
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.gameService.getGames().subscribe(observer);
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
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
    const dialogRef = this.dialog.open(GameDeletionConfirmationDialogComponent);

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteGame(game);
      }
    });
  }

  deleteGame(game: Game) {
    if(!game || !game.id) {
      console.log('Game ID is not valid.');
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        this.gamesList = response;
        this.dataSource = new MatTableDataSource<Game>(this.gamesList);
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
    const data = this.gamesList.slice();
    if (!sort.active || sort.direction === '') {
      this.sortedData = data;
      return;
    }

    this.sortedData = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case 'id':
          if(a.id && b.id) {
            return this.compare(a.id, b.id, isAsc);
          }
          return 1;
        case 'title':
          return this.compare(a.title, b.title, isAsc);
        case 'developer':
          return this.compare(a.developer, b.developer, isAsc);
        case 'publisher':
          return this.compare(a.publisher, b.publisher, isAsc);
        case 'releaseDate':
          return this.compare(a.releaseDate, b.releaseDate, isAsc);
          case 'releaseStatus':
            if(a.releaseStatus && b.releaseStatus) {
              return this.compare(a.releaseStatus.statusName, b.releaseStatus.statusName, isAsc);
            }
            return 1;
          case 'usersScore':
            return this.compare(a.usersScore, b.usersScore, isAsc);
        default:
          return 0;
      }
    });

    this.dataSource = new MatTableDataSource<Game>(this.sortedData);
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }
}
