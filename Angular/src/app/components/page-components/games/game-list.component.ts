import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, ViewChild } from '@angular/core';
import { GameService } from '../../../services/game.service';
import { Game } from '../../../interfaces/game';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, fromEvent, map, Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { MatSort } from '@angular/material/sort';
import { AuthService } from '../../../services/auth.service';
import { releaseStatuses } from '../../../enums/releaseStatuses';
import { BackgroundService } from '../../../services/background.service';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { DatePipe } from '@angular/common';
import { MatSelectChange } from '@angular/material/select';
import { Tag } from '../../../interfaces/tag';
import { TagService } from '../../../services/tag.service';
import { gameFilters } from '../../../interfaces/gameFilters';
import { GameFormDialogComponent } from './game-form-dialog.component';
import { FormBuilder, FormGroup } from '@angular/forms';
import { NotificationService } from '../../../services/notification.service';
import { ForumRequestService } from '../../../services/forumRequest.service';
import { ForumService } from '../../../services/forum.service';

@Component({
  selector: 'app-game-list',
  templateUrl: './game-list.component.html'
})
export class GameListComponent extends BaseAdComponent implements AfterViewInit {
  protected gamesList: Game[] = [];
  protected totalGames: number = 0;
  protected dataSource: MatTableDataSource<Game> = new MatTableDataSource<Game>(this.gamesList);
  protected displayedColumns: string[] = ['id', 'title', 'developer', 'publisher', 'releaseDate', 'releaseStatus', 'usersScore', 'tags', 'description', 'options'];

  protected releaseStatuses = releaseStatuses;
  protected tagList: Tag[] = [];
  private filters: gameFilters = {};
  protected filterForm: FormGroup;
  
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild('searchInput', { static: true }) searchInput?: ElementRef;

  constructor(
    private gameService: GameService,
    private forumRequestService: ForumRequestService,
    private forumService: ForumService,
    private fb: FormBuilder,
    private router: Router,
    private dialog: MatDialog,
    protected authService: AuthService,
    private tagService: TagService,
    private datePipe: DatePipe,
    private notificationService: NotificationService,
    private backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);

    this.filterForm = this.fb.group({
      dateRange: this.fb.group({
        start: [null],
        end: [null]
      }),
      releaseStatuses: [null],
      tags: [null],
      userScore: this.fb.group({
        min: [null],
        max: [null]
      }),
      search: [null]
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    
    this.backgroundService.setClasses(['fallingCds']);
    this.loadTags();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.loadGames();
    
    this.paginator.page.subscribe(() => this.loadGames());
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadGames();
    });

    if (this.searchInput) {
      fromEvent(this.searchInput.nativeElement, 'input').pipe(
        map((event: any) => event.target.value),
        debounceTime(300),
        distinctUntilChanged()

      ).subscribe(value => {
        this.onSearchChange(value);
      });
    }
  }

  loadTags() {
    this.tagService.getTags().subscribe({
      next: (response) => {
        if (response) {
          this.tagList = response;
        }
      }
    });
  }

  loadGames() {
    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = this.sort.active || 'id';
    const sortDir = this.sort.direction || 'asc';

    const observer: Observer<any> = {
      next: response => {
        if (response) {
          this.totalGames = response.totalElements;
          this.dataSource = new MatTableDataSource<Game>(response.content);
        } else {
          this.totalGames = 0;
          this.dataSource = new MatTableDataSource<Game>([]);
        }
      },
      error: error => {
        this.totalGames = 0;
        this.dataSource = new MatTableDataSource<Game>([]);
        console.error(error);
      },
      complete: () => {}
    };
    this.gameService.getGames(page, size, sortBy, sortDir, this.filters).subscribe(observer);
  }

  openAddNewGameDialog() {
    const dialogRef = this.dialog.open(GameFormDialogComponent, {
      data: {
        editing: false
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        this.loadGames();
      }
    });
  }

  openEditGameDialog(game: Game) {
    const dialogRef = this.dialog.open(GameFormDialogComponent, {
      data: {
        editing: true,
        game: game
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        this.loadGames();
      }
    });
  }

  routeToViewGame(title: string) {
    this.router.navigate(['/game/' + title]);
  }

  openGameDeletionConfirmationDialog(game: Game) {
    const dialogTitle = 'Game deletion';
    const dialogContent = 'Are you sure you want to delete the game ' + game.title + '?';
    const submitText = 'Delete';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteGame(game);
      }
    });
  }

  deleteGame(game: Game) {
    if (!game || !game.id) {
      console.log('Game ID is not valid.');
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        if (response) {
          this.dataSource.data = response;
          this.loadGames();
          this.notificationService.popSuccessToast('Deleted game successfuly');
        }
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
        }

        this.notificationService.popErrorToast('Deleting game failed', error);
      },
      complete: () => {}
    };

    this.gameService.deleteGame(game.id).subscribe(observer);
  }

  getTags(game: Game) {
    return game.tags?.map(tag => tag.tagName).join(', ');
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  findReleaseStatusName(status: string) {
    const releaseStatus = releaseStatuses.find(releaseStatus => releaseStatus.className === status);
    return releaseStatus ? releaseStatus.name : undefined;
  }

  // Filters

  onStartDateChange(event: MatDatepickerInputEvent<Date>) {
    const selectedDate = event.value;

    if (selectedDate) {
      const formattedDate = this.datePipe.transform(selectedDate, 'yyyy-MM-dd');
      
      if (formattedDate) {
        this.filters.startDate = formattedDate;
      }

      if (this.filters.startDate && this.filters.endDate) {
        this.loadGames();
      }
    }
  }

  onEndDateChange(event: MatDatepickerInputEvent<Date>) {
    const selectedDate = event.value;

    if (selectedDate) {
      const formattedDate = this.datePipe.transform(selectedDate, 'yyyy-MM-dd');

      if (formattedDate) {
        this.filters.endDate = formattedDate;
      }

      if (this.filters.startDate && this.filters.endDate) {
        this.loadGames();
      }
    }
  }

  onReleaseStatusesFilterChange(event: MatSelectChange) {
    this.filters.releaseStatus = event.value;
    this.loadGames();
  }

  onTagFilterChange(event: MatSelectChange) {
    this.filters.tags = event.value;
    this.loadGames();
  }

  onSearchChange(value: string) {
    this.filters.search = value;
    this.loadGames();
  }

  onScoreMinFilterChange(value: number) {
    this.filters.scoreMin = value;
    if (!this.filters.scoreMax) {
      this.filters.scoreMax = 10;
    }
    this.loadGames();
  }

  onScoreMaxFilterChange(value: number) {
    this.filters.scoreMax = value;
    if (!this.filters.scoreMin) {
      this.filters.scoreMin = 1;
    }
    this.loadGames();
  }

  clearFilters() {
    this.filters = {};
    this.filterForm.reset();
    this.filterForm.get('userScore')?.get('min')?.setValue(1);
    this.filterForm.get('userScore')?.get('max')?.setValue(10);
    this.loadGames();
  }
}
