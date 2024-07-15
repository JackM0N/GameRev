import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Game } from '../../../interfaces/game';
import { ReleaseStatus } from '../../../interfaces/releaseStatus';
import { Observer } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Toast, ToasterService } from 'angular-toaster';
import { GameService } from '../../../services/game.service';
import { ReleaseStatusService } from '../../../services/release-status.service';
import { Tag } from '../../../interfaces/tag';
import { TagService } from '../../../services/tag.service';

@Component({
  selector: 'app-games-form',
  templateUrl: './games-form.component.html',
  styleUrl: '/src/app/styles/shared-form-styles.css'
})
export class AddingGamesComponent implements OnInit {
  addingGameForm: FormGroup;
  releaseStatuses: ReleaseStatus[] = [];
  isEditRoute: boolean;
  listTitle: string = 'Add new game';
  tagsList: Tag[] = [];
  gameTitle: string = '';

  game: Game = {
    title: '',
    developer: '',
    publisher: '',
    releaseDate: new Date(),
    releaseStatus: undefined,
    description: '',
    tags: []
  };

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private toasterService: ToasterService,
    private gameService: GameService,
    private tagService: TagService,
    private releaseStatusService: ReleaseStatusService,
  ) {
    this.addingGameForm = this.formBuilder.group({
      title: [this.game.title, [Validators.required, Validators.minLength(1)]],
      developer: [this.game.developer, [Validators.required, Validators.minLength(1)]],
      publisher: [this.game.publisher, [Validators.required, Validators.minLength(1)]],
      releaseDate: [this.game.releaseDate],
      releaseStatus: [this.game.releaseStatus, [Validators.required, Validators.minLength(1)]],
      tags: [this.game.tags],
      description: [this.game.description, [Validators.required, Validators.minLength(1)]],
    });

    this.isEditRoute = this.route.snapshot.routeConfig?.path?.includes('/edit') == true;

    if (this.isEditRoute) {
      this.listTitle = 'Editing game';
    }

    const observerReleaseStatus: Observer<any> = {
      next: response => {
        this.releaseStatuses = response;
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.releaseStatusService.getReleaseStatuses().subscribe(observerReleaseStatus);

    const observerTag: Observer<any> = {
      next: response => {
        this.tagsList = response;
        this.tagsList.sort((a, b) => a.priority - b.priority);
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
          this.game = game;

          var fixedDate = game.releaseDate;
          if(game.releaseDate) {
            fixedDate = new Date(game.releaseDate);
          }

          var releaseStatus = game.releaseStatus;
          if (game.releaseStatus) {
            for (let i = 0; i < this.releaseStatuses.length; i++) {
              if (this.releaseStatuses[i].id === game.releaseStatus['id']) {
                releaseStatus = this.releaseStatuses[i];
                break;
              }
            }
          }

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
            releaseDate: fixedDate,
            releaseStatus: releaseStatus,
            tags: tags,
            description: game.description
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

      if (this.isEditRoute) {
        const observer: Observer<any> = {
          next: response => {
            this.router.navigate(['/games']);
            var toast: Toast = {
              type: 'success',
              title: 'Edited game successfuly',
              showCloseButton: true
            };
            this.toasterService.pop(toast);
          },
          error: error => {
            console.error(error);
            var toast: Toast = {
              type: 'error',
              title: 'Editing game failed',
              showCloseButton: true
            };
            this.toasterService.pop(toast);
          },
          complete: () => {}
        };
        this.gameService.editGame(this.gameTitle, gameData).subscribe(observer);
        return;
      }

      const observer: Observer<any> = {
        next: response => {
          this.router.navigate(['/games']);
          var toast: Toast = {
            type: 'success',
            title: 'Added game successfuly',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        error: error => {
          console.error(error);
          var toast: Toast = {
            type: 'error',
            title: 'Adding game failed',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        complete: () => {}
      };
      this.gameService.addGame(gameData).subscribe(observer);
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

  trackByFn(index: number, item: any): number {
    return item.id;
  }
}
