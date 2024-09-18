import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ReleaseStatus } from '../../../interfaces/releaseStatus';
import { Game } from '../../../interfaces/game';
import { Observer } from 'rxjs';
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
  protected addingGameForm: FormGroup;
  protected releaseStatuses: ReleaseStatus[] = releaseStatuses;
  protected listTitle: string = 'Add a new game';
  protected tagsList: Tag[] = [];
  private gameDate: Date = new Date();
  
  private game: Game = {
    title: '',
    developer: '',
    publisher: '',
    releaseDate: [],
    description: '',
    tags: [],
    usersScore: 0
  };

  constructor(
    private formBuilder: FormBuilder,
    private notificationService: NotificationService,
    private gameService: GameService,
    private tagService: TagService,
    protected dialogRef: MatDialogRef<GameFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) protected data: {
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

      var status = this.releaseStatuses.find(rs => rs.name === this.game.releaseStatus);

      if (status) {
        this.addingGameForm.patchValue({
          releaseStatus: status,
        });
      }
    }
  }

  onSubmit() {
    if (this.addingGameForm.valid) {
      const gameData = {
        ...this.game,
        ...this.addingGameForm.value
      };

      gameData.releaseDate = [gameData.releaseDate.getFullYear(), gameData.releaseDate.getMonth() + 1, gameData.releaseDate.getDate()];
      gameData.releaseStatus = gameData.releaseStatus.className;

      if (this.data.editing) {
        if (this.game.title) {
          this.gameService.editGame(this.game.title, gameData).subscribe({
            next: () => { this.notificationService.popSuccessToast('Edited game successfuly'); },
            error: error => this.notificationService.popErrorToast('Editing game failed', error)
          });
          this.dialogRef.close(true);
        }
        return;
      }

      this.gameService.addGame(gameData).subscribe({
        next: () => { this.notificationService.popSuccessToast('Added game successfuly'); },
        error: error => this.notificationService.popErrorToast('Adding game failed', error)
      });
      this.dialogRef.close(true);
    }
  }

  loadTags() {
    const observerTag: Observer<any> = {
      next: response => {
        if (response) {
          this.tagsList = response;
          this.tagsList.sort((a, b) => b.priority - a.priority);
  
          var tags = [];
          if (this.game.tags) {
            for (let i = 0; i < this.tagsList.length; i++) {
              for (let j = 0; j < this.game.tags.length; j++) {
                if (this.tagsList[i].id == this.game.tags[j].id) {
                  tags.push(this.tagsList[i]);
                }
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
