import { ChangeDetectorRef, Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { Game } from '../../../../models/game';
import { ActivatedRoute, Router } from '@angular/router';
import { GameService } from '../../../../services/game.service';
import { Location } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { formatDateArray } from '../../../../util/formatDate';
import { releaseStatuses } from '../../../../enums/releaseStatuses';
import { ReleaseStatus } from '../../../../models/releaseStatus';
import { BackgroundService } from '../../../../services/background.service';
import { BaseAdComponent } from '../../../base-components/base-ad-component';
import { AdService } from '../../../../services/ad.service';
import { GameInfoReviewListComponent } from './review-list.component';
import { LibraryFormDialogComponent } from '../../library/library-form-dialog.component';
import { AuthService } from '../../../../services/auth.service';
import { PopupDialogComponent } from '../../../general-components/popup-dialog.component';
import { NotificationService } from '../../../../services/notification.service';
import { ForumRequestService } from '../../../../services/forumRequest.service';
import { ForumService } from '../../../../services/forum.service';
import { GameFormDialogComponent } from '../game-form-dialog.component';

@Component({
  selector: 'app-game-information',
  templateUrl: './game-information.component.html'
})
export class GameInformationComponent extends BaseAdComponent implements OnInit, AfterViewInit {
  @ViewChild(GameInfoReviewListComponent) protected reviewListComponent!: GameInfoReviewListComponent;
  
  protected formatDate = formatDateArray;
  protected releaseStatuses: ReleaseStatus[] = releaseStatuses;
  protected usersScoreText = '';

  protected gameTitle = '';

  protected toggleSmallScreenMenu = false;

  protected game: Game = {
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
    private route: ActivatedRoute,
    private gameService: GameService,
    private _location: Location,
    protected dialog: MatDialog,
    protected authService: AuthService,
    protected forumRequestService: ForumRequestService,
    protected forumService: ForumService,
    protected backgroundService: BackgroundService,
    private router: Router,
    private notificationService: NotificationService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
  }

  override ngOnInit() {
    super.ngOnInit();
    
    this.backgroundService.setClasses(['fallingCds']);

    this.route.params.subscribe(params => {
      if (params['name']) {
        this.gameTitle = params['name'].replace(' ', '-');

        // Load game information
        this.gameService.getGameByName(this.gameTitle).subscribe((game: Game) => {
          this.game = game;

          // Replace release status with status from the list so it shows up in select
          for (const status of this.releaseStatuses) {
            if (status.className === this.game.releaseStatus) {
              this.game.releaseStatus = status.name;
              break;
            }
          }

          this.updateUsersScoreText();

          if (this.game.usersScore && this.game.usersScore > 0) {
            this.usersScoreText = "Users score: " + this.game.usersScore;
          } else {
            this.usersScoreText = "No reviews yet";
          }
        });
      }
    });
  }

  ngAfterViewInit() {
    setTimeout(() => {
      if (this.reviewListComponent) {
        this.reviewListComponent.usersScoreUpdated.subscribe(newScore => {
          this.updateUsersScoreText(newScore);
        });
      }
    }, 0);
  }

  updateUsersScoreText(score?: number) {
    if (this.game.usersScore) {
      let calculatedScore = this.game.usersScore;

      if (score) {
        calculatedScore = calculatedScore - score;
      }
  
      if (calculatedScore > 0) {
        this.usersScoreText = "Users score: " + calculatedScore;
      } else {
        this.usersScoreText = "No reviews yet";
      }
    }
  }

  goBack() {
    this._location.back();
  }

  openLibraryFormDialog(): void {
    const dialogRef = this.dialog.open(LibraryFormDialogComponent, {
      width: '400px',
      data: {
        editing: false,
        userGame: {
          game: this.game,
          completionStatus: undefined,
          isFavourite: false
        }
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log('The dialog was closed with result: ', result);
      }
    });
  }

  openGameDeletionConfirmationDialog() {
    const dialogTitle = 'Game deletion';
    const dialogContent = 'Are you sure you want to delete the game ' + this.game.title + '?';
    const submitText = 'Delete';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteGame(this.game);
      }
    });
  }

  deleteGame(game: Game) {
    if (!game || !game.id) {
      console.log('Game ID is not valid.');
      return;
    }

    this.gameService.deleteGame(game.id).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Deleted game successfuly');
        this.router.navigate(['/games']);
      },
      error: error => {
        if (error.error == "Game is used in forums") {
          const dialogTitle = 'Deleting game failed';
          const dialogContent = 'There are forums that are related to this game. Please delete them first.';

          this.dialog.open(PopupDialogComponent, {
            width: '400px',
            data: { dialogTitle, dialogContent, noSubmitButton: true }
          });
          return;

        } else if (error.error == "Game is used in forum requests") {
          const dialogTitle = 'Deleting game failed';
          const dialogContent = 'There are forum requests that are related to this game. Please delete them first.';

          this.dialog.open(PopupDialogComponent, {
            width: '400px',
            data: { dialogTitle, dialogContent, noSubmitButton: true }
          });
          return;
        }

        this.notificationService.popErrorToast('Deleting game failed', error);
      }
    });
  }

  openEditGameDialog() {
    const dialogRef = this.dialog.open(GameFormDialogComponent, {
      data: {
        editing: true,
        game: this.game
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        this.gameService.getGameByName(this.gameTitle).subscribe((game: Game) => {
          this.game = game;
        });
      }
    });
  }
}
