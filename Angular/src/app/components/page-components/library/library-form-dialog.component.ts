import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CompletionStatus } from '../../../interfaces/completionStatus';
import { UserGame } from '../../../interfaces/userGame';
import { completionStatuses } from '../../../enums/completionStatuses';
import { MatTableDataSource } from '@angular/material/table';
import { AuthService } from '../../../services/auth.service';
import { LibraryService } from '../../../services/library.service';
import { NotificationService } from '../../../services/notification.service';
import { Observer } from 'rxjs';
import { GameService } from '../../../services/game.service';
import { Game } from '../../../interfaces/game';

@Component({
  selector: 'app-library-form-dialog',
  templateUrl: './library-form-dialog.component.html',
})
export class LibraryFormDialogComponent implements OnInit {
  public libraryForm: FormGroup;
  public completionStatuses: CompletionStatus[] = completionStatuses;
  public gameList: Game[] = [];

  private userGame: UserGame = {
    id: undefined,
    user: undefined,
    game: undefined,
    completionStatus: undefined,
    isFavourite: false
  };
  
  constructor(
    private gameService: GameService,
    private libraryService: LibraryService,
    private authService: AuthService,
    private notificationService: NotificationService,
    public dialogRef: MatDialogRef<LibraryFormDialogComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: {
      dialogTitle: string,
      editing: boolean,
      userGame: UserGame,
      existingGames: UserGame[]
    }
  ) {
    this.libraryForm = this.formBuilder.group({
      completionStatus: [this.userGame.completionStatus, [Validators.required, Validators.minLength(1)]],
      isFavourite: [this.userGame.isFavourite, [Validators.required]],
      game: [{value: this.userGame.game, disabled: this.data.editing}, [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.userGame.user = { username: this.authService.getUsername() };

    this.loadGames();

    if (this.data.userGame) {
      this.userGame = this.data.userGame;

      this.completionStatuses.forEach((status) => {
        if (status.className == this.userGame.completionStatus) {
          this.userGame.completionStatus = status.className;
          this.libraryForm.patchValue({
            completionStatus: status.className
          });
        }
      });

      this.completionStatuses.forEach((status) => {
        if (status.className == this.userGame.completionStatus) {
          this.userGame.completionStatus = status.className;
          this.libraryForm.patchValue({
            completionStatus: status.className
          });
        }
      });
    }
  }

  loadGames() {
    const observer: Observer<any> = {
      next: response => {
        this.gameList = response.content;

        if (this.data.existingGames) {
          const existingGameTitles = new Set(this.data.existingGames.map(game => game.game?.title));
          this.gameList = this.gameList.filter(game => !existingGameTitles.has(game.title));

        } else {
          this.userGame.game = this.data.userGame.game;

          this.gameList.forEach((game) => {
            if (game.id == this.userGame.game?.id) {
              this.userGame.game = game;
              this.libraryForm.patchValue({
                game: game
              });
            }
          });
        }
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.gameService.getGames().subscribe(observer);
  }

  addUserGame() {
    const userGame = {
      ...this.userGame,
      ...this.libraryForm.value
    };

    this.libraryService.addUserGame(userGame).subscribe({
      next: () => { this.notificationService.popSuccessToast('Game added successfully'); },
      error: error => this.notificationService.popErrorToast('Game adding failed', error)
    });
  }

  editUserGame() {
    const userGame = {
      ...this.userGame,
      ...this.libraryForm.value
    };

    this.libraryService.updateUserGame(userGame).subscribe({
      next: () => { this.notificationService.popSuccessToast('Game edited successfully'); },
      error: error => this.notificationService.popErrorToast('Game editing failed', error)
    });
  }

  onConfirm(): void {
    if (this.data.editing) {
      this.editUserGame();
    } else {
      this.addUserGame();
    }

    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  isCompletionStatusInvalid() {
    const completionStatusControl = this.libraryForm.get('completionStatus');

    if (!completionStatusControl) {
      return true;
    }

    return completionStatusControl.hasError('required') && completionStatusControl.touched
  }

  isGameInvalid() {
    const gameControl = this.libraryForm.get('game');

    if (!gameControl) {
      return true;
    }

    return gameControl.hasError('required') && gameControl.touched
  }
}