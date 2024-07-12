import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { GameService } from '../../../services/game.service';
import { Game } from '../../../interfaces/game';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { Observer } from 'rxjs';

@Component({
  selector: 'app-games-list',
  templateUrl: './games-list.component.html',
  styleUrl: '/src/app/styles/shared-list-styles.css'
})
export class ViewingGamesComponent implements AfterViewInit, OnInit {
  gamesList: Game[] = [];
  dataSource: MatTableDataSource<Game> = new MatTableDataSource<Game>(this.gamesList);
  displayedColumns: string[] = ['id', 'title', 'developer', 'publisher', 'releaseStatus', 'tags', 'description', 'options'];
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private gameService: GameService,
    private router: Router
  ) {
  }

  ngOnInit() {
    const observer: Observer<any> = {
      next: response => {
        console.log(response);
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

  deleteGame(game: Game) {
    console.log('Delete game: ', game);
  }

  getTags(game: Game) {
    return game.tags.map(tag => tag).join(', ');
  }
}
