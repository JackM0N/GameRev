import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { GameService } from '../../../services/game.service';
import { Game } from '../../../interfaces/game';
import { ForumService } from '../../../services/forum.service';
import { Forum } from '../../../interfaces/forum';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-forum-form-dialog',
  templateUrl: './forum-form-dialog.component.html',
})
export class ForumFormDialogComponent {
  public forumForm: FormGroup;
  public nameMinLength: number = 4;
  public descriptionMinLength: number = 8;
  public gameList: Game[] = [];
  public forumList: Forum[] = [];

  private description: string = '';
  private name: string = '';
  private game?: Game;
  private parentForumId?: number;
  private forumId?: number;
  
  constructor(
    private forumService: ForumService,
    private gameService: GameService,
    private notificationService: NotificationService,
    public dialogRef: MatDialogRef<ForumFormDialogComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data?: {
      id?: number,
      name?: string,
      description?: string,
      gameTitle?: string,
      parentForumId?: number,
      editing?: boolean
    }
  ) {
    this.forumForm = this.formBuilder.group({
      parentForum: [this.parentForumId, [Validators.required]],
      name: [this.name, [Validators.required, Validators.minLength(this.nameMinLength)]],
      description: [this.description, [Validators.required, Validators.minLength(this.descriptionMinLength)]],
      game: [this.game, [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadGames();

    if (this.data) {
      if (this.data.id) {
        this.forumId = this.data.id;
      }

      if (this.data.name) {
        this.name = this.data.name;
        this.forumForm.patchValue({
          name: this.data.name
        });
      }

      if (this.data.description) {
        this.description = this.data.description;
        this.forumForm.patchValue({
          description: this.data.description
        });
      }

      if (this.data.parentForumId) {
        this.parentForumId = this.data.parentForumId;
      } else {
        this.parentForumId = 1;
      }
    }

    this.loadForums();
  }

  loadGames() {
    this.gameService.getGames().subscribe({
      next: (response: any) => {
        this.gameList = response.content;

        if (this.data && this.data.gameTitle) {
          const findGame = this.gameList.find(game => game.title === this.data?.gameTitle);
  
          if (findGame) {
            this.game = findGame;
            this.forumForm.patchValue({
              game: findGame
            });
          } else {
            console.error('Game not found');
          }
        }
      },
      error: (error: any) => { console.error(error); }
    });
  }

  loadForums() {
    this.forumList = [];

    this.forumService.getForum(this.parentForumId).subscribe({
      next: (response: any) => {
        if (response) {
          this.forumList = response.content;

          if (this.parentForumId) {
            const findForum = this.forumList.find(forum => forum.id == this.parentForumId);
    
            if (findForum) {
              this.forumForm.patchValue({
                parentForum: findForum
              });
            } else {
              console.error('Forum not found');
            }
          }
        }
      },
      error: (error: any) => { console.error(error); }
    });
  }

  isGameInvalid() {
    const gameControl = this.forumForm.get('game');

    if (!gameControl) {
      return true;
    }

    return gameControl.hasError('required') && gameControl.touched
  }

  submitForm() {
    if (this.forumForm.valid) {
      const newForum: Forum = {
        forumName: this.forumForm.get('name')?.value,
        description: this.forumForm.get('description')?.value,
        gameTitle: this.forumForm.get('game')?.value.title,
        parentForumId: this.forumForm.get('parentForum')?.value.id,
      }

      if (this.data && this.data.editing) {
        newForum.id = this.forumId;
        this.forumService.editForum(newForum).subscribe({
          next: () => { this.notificationService.popSuccessToast('Forum edited'); },
          error: error => this.notificationService.popErrorToast('Forum editing failed', error)
        });
        this.dialogRef.close(true);
        return;
      }

      if (!this.parentForumId) {
        console.error('Parent forum is not set');
        return;
      }

      this.forumService.addForum(newForum).subscribe({
        next: () => { this.notificationService.popSuccessToast('Forum added'); },
        error: error => this.notificationService.popErrorToast('Forum adding failed', error)
      });
      this.dialogRef.close(true);
    }
  }

  onConfirm(): void {
    this.submitForm();
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
