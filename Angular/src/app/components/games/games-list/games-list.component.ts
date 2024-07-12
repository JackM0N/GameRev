import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { GameService } from '../../../services/game.service';
import { Game } from '../../../interfaces/game';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { GameDeletionConfirmationDialogComponent } from '../game-deletion-confirmation-dialog/game-deletion-confirmation-dialog.component';

@Component({
  selector: 'app-games-list',
  templateUrl: './games-list.component.html',
  styleUrl: '/src/app/styles/shared-list-styles.css'
})
export class ViewingGamesComponent implements AfterViewInit, OnInit {
  gamesList: Game[] = [];
  dataSource: MatTableDataSource<Game> = new MatTableDataSource<Game>(this.gamesList);
  displayedColumns: string[] = ['id', 'title', 'developer', 'publisher', 'releaseDate', 'releaseStatus', 'tags', 'description', 'options'];
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
}
