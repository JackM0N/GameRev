import { Component, OnInit } from '@angular/core';
import { Game } from '../../../interfaces/game';
import { ActivatedRoute, Router } from '@angular/router';
import { GameService } from '../../../services/game.service';

@Component({
  selector: 'app-games-form',
  templateUrl: './game-information.component.html',
  styleUrl: './game-information.component.css'
})
export class GameInformationComponent implements OnInit {
  game: Game = {
    title: '',
    developer: '',
    publisher: '',
    releaseDate: [],
    releaseStatus: undefined,
    description: '',
    tags: [],
    usersScore: 0
  };

  constructor(
    private route: ActivatedRoute,
    private gameService: GameService,
    private router: Router,
  ) {
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['name']) {
        this.gameService.getGameByName(params['name']).subscribe((game: Game) => {
          this.game = game;
        });
      }
    });
  }

  formatDate(dateArray: number[] | undefined): string {
    if (!dateArray) {
      return 'Unknown';
    }

    if (dateArray.length !== 3) {
      return 'Invalid date';
    }

    const [year, month, day] = dateArray;
    const date = new Date(year, month - 1, day, 15);

    if (isNaN(date.getTime())) {
      return 'Invalid date';
    }

    return new Intl.DateTimeFormat('en-US', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    }).format(date);
  }

  goBack() {
    this.router.navigate(['/games']);
  }
}
