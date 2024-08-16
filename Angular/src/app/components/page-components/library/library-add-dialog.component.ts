import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CompletionStatus } from '../../../interfaces/completionStatus';
import { UserGame } from '../../../interfaces/userGame';
import { completionStatuses } from '../../../interfaces/completionStatuses';
import { Game } from '../../../interfaces/game';
import { GameService } from '../../../services/game.service';
import { Observer } from 'rxjs';

@Component({
  selector: 'app-library-add-dialog',
  templateUrl: './library-add-dialog.component.html',
})
export class LibraryAddDialogComponent implements OnInit {
  addForm: FormGroup;
  completionStatus?: CompletionStatus = undefined;
  completionStatuses: CompletionStatus[] = completionStatuses;
  isFavourite: boolean = false;
  gameList: Game[] = [];
  game?: Game = undefined;
  
  constructor(
    public dialogRef: MatDialogRef<LibraryAddDialogComponent>,
    private formBuilder: FormBuilder,
    private gameService: GameService,
    @Inject(MAT_DIALOG_DATA) public data: { dialogTitle: string, existingGames: UserGame[] }
  ) {
    this.addForm = this.formBuilder.group({
      game: [this.game, [Validators.required, Validators.minLength(1)]],
      completionStatus: [this.completionStatus, [Validators.required, Validators.minLength(1)]],
      isFavourite: [this.isFavourite, [Validators.required]]
    });
  }

  ngOnInit(): void {
    const observer: Observer<any> = {
      next: response => {
        this.gameList = response.content;

        const existingGameTitles = new Set(this.data.existingGames.map(game => game.game.title));
        this.gameList = this.gameList.filter(game => !existingGameTitles.has(game.title));
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.gameService.getGames().subscribe(observer);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  isCompletionStatusInvalid() {
    const completionStatusControl = this.addForm.get('completionStatus');

    if (!completionStatusControl) {
      return true;
    }

    return completionStatusControl.hasError('required') && completionStatusControl.touched
  }

  isGameInvalid() {
    const gameControl = this.addForm.get('game');

    if (!gameControl) {
      return true;
    }

    return gameControl.hasError('required') && gameControl.touched
  }
}
