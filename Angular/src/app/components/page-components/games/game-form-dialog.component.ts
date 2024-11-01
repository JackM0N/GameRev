import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ReleaseStatus } from '../../../models/releaseStatus';
import { Game } from '../../../models/game';
import { releaseStatuses } from '../../../enums/releaseStatuses';
import { Tag } from '../../../models/tag';
import { GameService } from '../../../services/game.service';
import { NotificationService } from '../../../services/notification.service';
import { TagService } from '../../../services/tag.service';

@Component({
  selector: 'app-game-form-dialog',
  templateUrl: './game-form-dialog.component.html',
})
export class GameFormDialogComponent implements OnInit {
  protected addingGameForm: FormGroup;
  protected releaseStatuses: ReleaseStatus[] = releaseStatuses;
  protected listTitle = 'Add a new game';
  protected tagsList: Tag[] = [];

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
      title: [undefined, [Validators.required, Validators.minLength(1)]],
      developer: [undefined, [Validators.required, Validators.minLength(1)]],
      publisher: [undefined, [Validators.required, Validators.minLength(1)]],
      releaseDate: [undefined],
      releaseStatus: [undefined, [Validators.required, Validators.minLength(1)]],
      tags: [undefined],
      description: [undefined, [Validators.required, Validators.minLength(1)]],
      usersScore: [undefined]
    });

    if (this.data.editing) {
      this.listTitle = 'Editing game';
    }

    this.loadTags();
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
  
  ngOnInit() {
    if (this.data.game) {
      this.addingGameForm.patchValue({
        ...this.data.game,
        releaseDate: new Date(this.data.game.releaseDate[0], this.data.game.releaseDate[1] -1, this.data.game.releaseDate[2], 15)
      });

      const status = this.releaseStatuses.find(rs => rs.className === this.data.game.releaseStatus || rs.name === this.data.game.releaseStatus);

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
        ...this.addingGameForm.value
      };

      gameData.releaseDate = [gameData.releaseDate.getFullYear(), gameData.releaseDate.getMonth() + 1, gameData.releaseDate.getDate()];
      gameData.releaseStatus = gameData.releaseStatus.className;

      if (this.data.editing) {
        if (this.data.game.title) {
          this.gameService.editGame(this.data.game.title, gameData).subscribe({
            next: () => { this.notificationService.popSuccessToast('Edited game successfuly'); },
            error: error => this.notificationService.popErrorToast('Editing game failed', error)
          });
        }
        this.dialogRef.close(true);
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
    this.tagService.getTags().subscribe({
      next: response => {
        if (response) {
          this.tagsList = response;
          this.tagsList.sort((a, b) => b.priority - a.priority);
  
          const tags: any = [];
          const formTags = this.addingGameForm.get('tags');
          if (formTags && formTags.value) {
            formTags.value.forEach((tag: any) => {
              const matchingTag = this.tagsList.find((t: any) => t.id === tag.id);
              if (matchingTag) {
                tags.push(matchingTag);
              }
            });
          }
          
          this.addingGameForm.patchValue({
            tags: tags,
          });
        }
      },
      error: error => { console.error(error); }
    });
  }

  isReleaseStatusInvalid() {
    const releaseStatusControl = this.addingGameForm.get('releaseStatus');
    if (!releaseStatusControl) {
      return true;
    }
    return releaseStatusControl.hasError('required') && releaseStatusControl.touched
  }
}
