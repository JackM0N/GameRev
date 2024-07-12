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

  game: Game = {
    title: '',
    developer: '',
    publisher: '',
    releaseDate: '',
    releaseStatus: -1,
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

    if(this.isEditRoute) {
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
        this.gameService.getGameByName(params['name']).subscribe((game: Game) => {
          this.game = game;
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

      const observer: Observer<any> = {
        next: response => {
          this.router.navigate(['/']);
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

      console.log(gameData);
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
