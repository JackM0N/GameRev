import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { Game } from '../../../../interfaces/game';
import { ActivatedRoute } from '@angular/router';
import { GameService } from '../../../../services/game.service';
import { Location } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { formatDateArray } from '../../../../util/formatDate';
import { releaseStatuses } from '../../../../enums/releaseStatuses';
import { ReleaseStatus } from '../../../../interfaces/releaseStatus';
import { BackgroundService } from '../../../../services/background.service';
import { BaseAdComponent } from '../../../base-components/base-ad-component';
import { AdService } from '../../../../services/ad.service';
import { GameInfoReviewListComponent } from './review-list.component';
import { LibraryFormDialogComponent } from '../../library/library-form-dialog.component';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-game-information',
  templateUrl: './game-information.component.html'
})
export class GameInformationComponent extends BaseAdComponent implements OnInit {
  @ViewChild(GameInfoReviewListComponent) protected reviewListComponent!: GameInfoReviewListComponent;
  
  protected formatDate = formatDateArray;
  protected releaseStatuses: ReleaseStatus[] = releaseStatuses;
  protected likeColor: 'primary' | '' = '';
  protected dislikeColor: 'warn' | '' = '';
  protected usersScoreText: string = '';

  protected gameTitle: string = '';

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
    protected backgroundService: BackgroundService,
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

          this.releaseStatuses.forEach(status => {
            if (status.className === this.game.releaseStatus) {
              this.game.releaseStatus = status.name;
            }
          });

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
      var calculatedScore = this.game.usersScore;

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
}
