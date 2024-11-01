import { AfterViewInit, ChangeDetectorRef, Component, ViewChild, OnInit } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSort } from '@angular/material/sort';
import { LibraryService } from '../../../services/library.service';
import { AuthService } from '../../../services/auth.service';
import { UserGame } from '../../../models/userGame';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { BackgroundService } from '../../../services/background.service';
import { NotificationService } from '../../../services/notification.service';
import { completionStatuses } from '../../../enums/completionStatuses';
import { MatSelectChange } from '@angular/material/select';
import { libraryFilters } from '../../../filters/libraryFilters';
import { Tag } from '../../../models/tag';
import { TagService } from '../../../services/tag.service';
import { LibraryFormDialogComponent } from './library-form-dialog.component';

@Component({
  selector: 'app-library',
  templateUrl: './library.component.html'
})
export class LibraryComponent extends BaseAdComponent implements AfterViewInit, OnInit {
  private gamesList: UserGame[] = [];
  protected totalGames = 0;
  protected dataSource: MatTableDataSource<UserGame> = new MatTableDataSource<UserGame>(this.gamesList);
  protected libraryEmpty = false;
  protected displayedColumns: string[] = ['game', 'completionStatus', 'isFavourite', 'options'];
  private filters: libraryFilters = {};
  protected completionStatuses = completionStatuses;
  protected tagList: Tag[] = [];

  @ViewChild(MatPaginator) protected paginator!: MatPaginator;
  @ViewChild(MatSort) protected sort!: MatSort;

  constructor(
    private libraryService: LibraryService,
    private authService: AuthService,
    private notificationService: NotificationService,
    private tagService: TagService,
    protected dialog: MatDialog,
    protected backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
  }
  
  override ngOnInit(): void {
    super.ngOnInit();
    this.backgroundService.setClasses(['fallingCds']);
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.loadGames();
    this.loadTags();

    this.paginator.page.subscribe(() => this.loadGames());
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadGames();
    });
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
    const nickname = this.authService.getNickname();

    if (!nickname) {
      console.log('Nickname is not valid.');
      return;
    }

    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = this.sort.active || 'id';
    const sortDir = this.sort.direction || 'asc';

    this.libraryService.getUserGames(nickname, page, size, sortBy, sortDir, this.filters).subscribe({
      next: response => {
        if (response) {
          this.gamesList = response.content;
          this.totalGames = response.totalElements;
          this.dataSource = new MatTableDataSource<UserGame>(this.gamesList);
          this.dataSource.data = this.gamesList;
          this.libraryEmpty = (this.dataSource.data.length == 0);
        } else {
          this.gamesList = [];
          this.totalGames = 0;
          this.libraryEmpty = true;
        }
      },
      error: error => {
        console.error(error);
      }
    });
  }

  openEditUserGameDialog(userGame: UserGame) {
    const dialogRef = this.dialog.open(LibraryFormDialogComponent, {
      width: '300px',
      data: {
        editing: true,
        userGame: userGame
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        this.loadGames();
      }
    });
  }

  editUserGame(userGame: UserGame) {
    this.libraryService.updateUserGame(userGame).subscribe({
      next: () => { this.notificationService.popSuccessToast('Game updated successfully'); },
      error: error => this.notificationService.popErrorToast('Game updating failed', error)
    });
  }

  openAddUserGameDialog() {
    const dialogRef = this.dialog.open(LibraryFormDialogComponent, {
      width: '300px',
      data: {
        editing: false,
        existingGames: this.gamesList
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        /*
        this.gamesList.push(userGame);

        this.totalGames = this.totalGames + 1;
        this.dataSource = new MatTableDataSource<UserGame>(this.gamesList);
        this.dataSource.data = this.gamesList;
        */

        this.loadGames();
      }
    });
  }

  openGameDeletionConfirmationDialog(userGame: UserGame) {
    const dialogTitle = 'Game deletion';
    const dialogContent = 'Are you sure you want to delete the game ' + userGame?.game?.title + '?';
    const submitText = 'Delete';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteGame(userGame);
      }
    });
  }

  deleteGame(userGame: UserGame) {
    if (!userGame || !userGame.id) {
      console.log('Game ID is not valid.');
      return;
    }

    this.libraryService.deleteUserGame(userGame.id).subscribe({
      next: () => {
        this.dataSource.data = this.gamesList;
        this.loadGames();
      },
      error: error => {
        console.error(error);
      }
    });
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  findCompletionStatusName(status: string) {
    const completionStatus = completionStatuses.find(completionStatus => completionStatus.className === status);
    return completionStatus ? completionStatus.name : undefined;
  }

  // Filters

  onCompletionStatusFilterChange(event: MatSelectChange) {
    this.filters.completionStatus = event.value;
    this.loadGames();
  }

  onFavoriteFilterChange(event: MatSelectChange) {
    this.filters.isFavorite = event.value;
    this.loadGames();
  }

  onTagFilterChange(event: MatSelectChange) {
    this.filters.tags = event.value;
    this.loadGames();
  }
}
