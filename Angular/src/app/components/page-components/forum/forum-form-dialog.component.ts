import { Component, Inject, Input } from '@angular/core';
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
  @Input() editing: boolean = false;
  
  public forumForm: FormGroup;
  public nameMinLength: number = 4;
  public descriptionMinLength: number = 8;
  public gameList: Game[] = [];

  private description: string = '';
  private name: string = '';
  private game?: Game;
  private parentForumId?: number;
  
  constructor(
    private forumService: ForumService,
    private gameService: GameService,
    private notificationService: NotificationService,
    public dialogRef: MatDialogRef<ForumFormDialogComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data?: {
      name?: string,
      description?: string,
      game?: Game,
      parentForumId?: number
    }
  ) {
    this.forumForm = this.formBuilder.group({
      name: [this.name, [Validators.required, Validators.minLength(this.nameMinLength)]],
      description: [this.description, [Validators.required, Validators.minLength(this.descriptionMinLength)]],
      game: [this.game, [Validators.required]]
    });
  }

  ngOnInit(): void {
    if (this.data) {

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

      if (this.data.game) {
        this.game = this.data.game;
        this.forumForm.patchValue({
          game: this.data.game
        });
      }

      if (this.data.parentForumId) {
        this.parentForumId = this.data.parentForumId;
      }

    }
    this.loadGames();
  }

  loadGames() {
    this.gameService.getGames().subscribe({
      next: (response: any) => { this.gameList = response.content; },
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
      if (!this.parentForumId) {
        console.error('Parent forum is not set');
        return;
      }

      const newForum: Forum = {
        forumName: this.forumForm.get('name')?.value,
        description: this.forumForm.get('description')?.value,
        gameTitle: this.forumForm.get('game')?.value.title,
        parentForumId: this.parentForumId
      }

      this.forumService.addForum(newForum).subscribe({
        next: () => { this.notificationService.popSuccessToast('Forum added', true); },
        error: error => this.notificationService.popErrorToast('Forum adding failed', error)
      });
    } else {
      console.error('Form is invalid');
    }
  }

  onConfirm(): void {
    this.dialogRef.close(true);
    this.submitForm();
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
