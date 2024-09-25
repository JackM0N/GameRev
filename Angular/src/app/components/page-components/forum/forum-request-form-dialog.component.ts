import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { GameService } from '../../../services/game.service';
import { Game } from '../../../interfaces/game';
import { ForumService } from '../../../services/forum.service';
import { Forum } from '../../../interfaces/forum';
import { NotificationService } from '../../../services/notification.service';
import { ForumRequestService } from '../../../services/forumRequest.service';
import { ForumRequest } from '../../../interfaces/forumRequest';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-forum-request-form-dialog',
  templateUrl: './forum-request-form-dialog.component.html',
})
export class ForumRequestFormDialogComponent {
  protected forumForm: FormGroup;
  protected nameMinLength: number = 4;
  protected descriptionMinLength: number = 8;
  protected gameList: Game[] = [];
  protected forumList: Forum[] = [];

  private description: string = '';
  private requestId?: number;
  private name: string = '';
  private game?: Game;
  private parentForum?: Forum;
  
  constructor(
    private authService: AuthService,
    private forumService: ForumService,
    private forumRequestService: ForumRequestService,
    private gameService: GameService,
    private notificationService: NotificationService,
    protected dialogRef: MatDialogRef<ForumRequestFormDialogComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) protected data?: {
      id?: number,
      name?: string,
      description?: string,
      game?: Game,
      parentForum?: Forum,
      lockParentForum?: boolean,
      editing?: boolean
    }
  ) {
    this.forumForm = this.formBuilder.group({
      parentForum: [{value: this.parentForum, disabled: this.data?.lockParentForum && !this.authService.isAdmin}, [Validators.required]],
      name: [this.name, [Validators.required, Validators.minLength(this.nameMinLength)]],
      description: [this.description, [Validators.required, Validators.minLength(this.descriptionMinLength)]],
      game: [this.game, [Validators.required]]
    });
  }

  ngOnInit(): void {
    if (this.data) {
      if (this.data.id) {
        this.requestId = this.data.id;
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

      if (this.data.parentForum) {
        this.parentForum = this.data.parentForum;
      }
    }

    this.loadGames();
    this.loadForums();
  }

  loadGames() {
    this.gameService.getGames().subscribe({
      next: (response: any) => {
        this.gameList = response.content;

        if (this.data && this.data.game) {
          const findGame = this.gameList.find(game => game.id === this.data?.game?.id);
  
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

    const id = this.parentForum ? this.parentForum.id : 0;
    
    this.forumService.getForum(id).subscribe({
      next: (response: any) => {
        if (response) {
          this.forumList = response.content;
          
          if (this.parentForum && this.parentForum.id) {
            const findForum = this.forumList.find(forum => forum.id == this.parentForum?.id);
    
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

  submitFormRequest() {
    if (this.forumForm.valid) {
      const forumRequest: ForumRequest = {
        forumName: this.forumForm.get('name')?.value,
        description: this.forumForm.get('description')?.value,
        game: { id: this.forumForm.get('game')?.value.id },
        parentForum: this.forumForm.get('parentForum')?.value,
        author: { nickname: this.authService.getNickname() }
      }

      if (this.data && this.data.editing) {
        forumRequest.id = this.requestId;
        this.forumRequestService.editRequest(forumRequest).subscribe({
          next: () => { this.notificationService.popSuccessToast('Forum request edited'); },
          error: error => this.notificationService.popErrorToast('Forum request editing failed', error)
        });
        return;
      }

      this.forumRequestService.addRequest(forumRequest).subscribe({
        next: () => { this.notificationService.popSuccessToast('Forum request added'); },
        error: error => this.notificationService.popErrorToast('Forum request adding failed', error)
      });

    } else {
      console.error('Form is invalid');
    }
  }

  onConfirm(): void {
    this.dialogRef.close(true);
    this.submitFormRequest();
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
