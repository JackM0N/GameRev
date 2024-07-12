import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { GameService } from '../../../services/game.service';
import { Game } from '../../../interfaces/game';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';

@Component({
  selector: 'app-games-list',
  templateUrl: './games-list.component.html',
  styleUrl: '/src/app/styles/shared-list-styles.css'
})
export class ViewingGamesComponent implements AfterViewInit {
  gamesList: Game[];
  dataSource: MatTableDataSource<Game>;
  displayedColumns: string[] = ['game_id', 'title', 'developer', 'publisher', 'release_status', 'description', 'options'];
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private gameService: GameService,
    private router: Router
  ) {
    this.gamesList = [
      { game_id: 1, title: 'Game 1', developer: 'Developer 1', publisher: 'Publisher 1', release_status: 0, release_date: '2021-01-01', tags: [], description: 'Description 1' },
      { game_id: 2, title: 'Game 2', developer: 'Developer 2', publisher: 'Publisher 2', release_status: 1, release_date: '2021-02-02', tags: [], description: 'Description 2' },
      { game_id: 3, title: 'Game 3', developer: 'Developer 3', publisher: 'Publisher 3', release_status: 2, release_date: '2021-03-03', tags: [], description: 'Description 3' },
      { game_id: 4, title: 'Game 4', developer: 'Developer 4', publisher: 'Publisher 4', release_status: 3, release_date: '2021-04-04', tags: [], description: 'Description 4' },
      { game_id: 5, title: 'Game 5', developer: 'Developer 5', publisher: 'Publisher 5', release_status: 4, release_date: '2021-05-05', tags: [], description: 'Description 5' },
      { game_id: 6, title: 'Game 6', developer: 'Developer 5', publisher: 'Publisher 5', release_status: 4, release_date: '2021-05-05', tags: [], description: 'Description 5' },
      { game_id: 7, title: 'Game 7', developer: 'Developer 5', publisher: 'Publisher 5', release_status: 4, release_date: '2021-05-05', tags: [], description: 'Description 5' },
      { game_id: 8, title: 'Game 8', developer: 'Developer 5', publisher: 'Publisher 5', release_status: 4, release_date: '2021-05-05', tags: [], description: 'Description 5' },
      { game_id: 9, title: 'Game 9', developer: 'Developer 5', publisher: 'Publisher 5', release_status: 4, release_date: '2021-05-05', tags: [], description: 'Description 5' },

    ];
    this.dataSource = new MatTableDataSource<Game>(this.gamesList);
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

  deleteGame(game: Game) {
    console.log('Delete game: ', game);
  }
}
