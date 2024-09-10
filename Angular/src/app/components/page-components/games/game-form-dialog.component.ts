import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ReleaseStatus } from '../../../interfaces/releaseStatus';
import { Game } from '../../../interfaces/game';
import { Observer } from 'rxjs';
import { NotificationAction } from '../../../enums/notificationActions';
import { releaseStatuses } from '../../../enums/releaseStatuses';
import { Tag } from '../../../interfaces/tag';
import { GameService } from '../../../services/game.service';
import { NotificationService } from '../../../services/notification.service';
import { TagService } from '../../../services/tag.service';

@Component({
  selector: 'app-game-form-dialog',
  templateUrl: './game-form-dialog.component.html',
})
export class GameFormDialogComponent {
  public addingGameForm: FormGroup;
  public releaseStatuses: ReleaseStatus[] = releaseStatuses;
  public listTitle: string = 'Add a new game';
  public tagsList: Tag[] = [];
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
    private notificationService: NotificationService,
    private gameService: GameService,
    private tagService: TagService,
    public dialogRef: MatDialogRef<GameFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      editing: boolean;
      game: Game;
    }
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
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
  
  ngOnInit() {
    this.loadTags();

    if (this.data.editing) {
      this.listTitle = 'Editing game';
    }

    if (this.data.game) {
      this.game = this.data.game;

      if (this.game.releaseDate) {
        this.gameDate = new Date(this.game.releaseDate[0], this.game.releaseDate[1] -1, this.game.releaseDate[2], 15);
      }

      this.addingGameForm.patchValue({
        title: this.game.title,
        developer: this.game.developer,
        publisher: this.game.publisher,
        releaseDate: this.gameDate,
        releaseStatus: this.game.releaseStatus,
        description: this.game.description,
        usersScore: this.game.usersScore
      });
    }
  }

  onSubmit() {
    if (this.addingGameForm.valid) {
      const gameData = {
        ...this.game,
        ...this.addingGameForm.value
      };

      gameData.releaseDate = [gameData.releaseDate.getFullYear(), gameData.releaseDate.getMonth() + 1, gameData.releaseDate.getDate()];

      if (this.data.editing) {
        this.gameService.editGame(this.game.title, gameData).subscribe({
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

  loadTags() {
    const observerTag: Observer<any> = {
      next: response => {
        if (response) {
          this.tagsList = response;
          this.tagsList.sort((a, b) => b.priority - a.priority);
  
          var tags = [];
          for (let i = 0; i < this.tagsList.length; i++) {
            for (let j = 0; j < this.game.tags.length; j++) {
              if (this.tagsList[i].id == this.game.tags[j].id) {
                tags.push(this.tagsList[i]);
              }
            }
          }

          this.addingGameForm.patchValue({
            tags: tags,
          });
        }
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.tagService.getTags().subscribe(observerTag);
  }

  isReleaseStatusInvalid() {
    const releaseStatusControl = this.addingGameForm.get('releaseStatus');

    if (!releaseStatusControl) {
      return true;
    }

    return releaseStatusControl.hasError('required') && releaseStatusControl.touched
  }
}
