import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Game } from '../../../interfaces/game';
import { ReleaseStatus } from '../../../interfaces/releaseStatus';
import { Observer } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { GameService } from '../../../services/game.service';
import { Tag } from '../../../interfaces/tag';
import { TagService } from '../../../services/tag.service';
import { releaseStatuses } from '../../../enums/releaseStatuses';
import { NotificationService } from '../../../services/notification.service';
import { NotificationAction } from '../../../enums/notificationActions';

@Component({
  selector: 'app-game-form',
  templateUrl: './game-form.component.html',
  styleUrl: '/src/app/styles/shared-form-styles.css'
})
export class GameFormComponent implements OnInit {
  public addingGameForm: FormGroup;
  public releaseStatuses: ReleaseStatus[] = releaseStatuses;
  private isEditRoute: boolean;
  public listTitle: string = 'Add new game';
  public tagsList: Tag[] = [];
  private gameTitle: string = '';
  private gameDate: Date = new Date();

  private game: Game = {
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
    private formBuilder: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private notificationService: NotificationService,
    private gameService: GameService,
    private tagService: TagService
  ) {
    this.addingGameForm = this.formBuilder.group({
      title: [this.game.title, [Validators.required, Validators.minLength(1)]],
      developer: [this.game.developer, [Validators.required, Validators.minLength(1)]],
      publisher: [this.game.publisher, [Validators.required, Validators.minLength(1)]],
      releaseDate: [this.game.releaseDate],
      releaseStatus: [this.game.releaseStatus, [Validators.required, Validators.minLength(1)]],
      tags: [this.game.tags],
      description: [this.game.description, [Validators.required, Validators.minLength(1)]],
      usersScore: [this.game.usersScore]
    });

    this.isEditRoute = this.route.snapshot.routeConfig?.path?.includes('/edit') == true;

    if (this.isEditRoute) {
      this.listTitle = 'Editing game';
    }

    const observerTag: Observer<any> = {
      next: response => {
        this.tagsList = response;
        this.tagsList.sort((a, b) => b.priority - a.priority);
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.tagService.getTags().subscribe(observerTag);
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['name']) {
        this.gameTitle = params['name'];
        this.gameService.getGameByName(this.gameTitle).subscribe((game: Game) => {
          if (game.releaseDate) {
            this.gameDate = new Date(game.releaseDate[0], game.releaseDate[1] -1, game.releaseDate[2], 15);
          }
          this.game = game;

          var tags = [];
          for (let i = 0; i < this.tagsList.length; i++) {
            for (let j = 0; j < game.tags.length; j++) {
              if(this.tagsList[i].id === game.tags[j].id) {
                tags.push(this.tagsList[i]);
              }
            }
          }

          this.addingGameForm.patchValue({
            title: game.title,
            developer: game.developer,
            publisher: game.publisher,
            releaseDate: this.gameDate,
            releaseStatus: game.releaseStatus,
            tags: tags,
            description: game.description,
            usersScore: game.usersScore
          });
        });
      }
    });
  }

  onSubmit() {
    if (this.addingGameForm.valid) {
      const gameData = {
        ...this.game,
        ...this.addingGameForm.value
      };

      gameData.releaseDate = [gameData.releaseDate.getFullYear(), gameData.releaseDate.getMonth() + 1, gameData.releaseDate.getDate()];

      if (this.isEditRoute) {
        this.gameService.editGame(this.gameTitle, gameData).subscribe({
          next: () => { this.notificationService.popSuccessToast('Edited game successfuly', NotificationAction.GO_BACK); },
          error: error => this.notificationService.popErrorToast('Editing game failed', error)
        });
        return;
      }

      this.gameService.addGame(gameData).subscribe({
        next: () => { this.notificationService.popSuccessToast('Added game successfuly', NotificationAction.GO_BACK); },
        error: error => this.notificationService.popErrorToast('Adding game failed', error)
      });
    }
  }

  cancel() {
    this.router.navigate(['/games']);
  }

  isReleaseStatusInvalid() {
    const releaseStatusControl = this.addingGameForm.get('releaseStatus');

    if (!releaseStatusControl) {
      return true;
    }

    return releaseStatusControl.hasError('required') && releaseStatusControl.touched
  }
}
