import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, ViewChild } from '@angular/core';
import { BackgroundService } from '../../../services/background.service';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { ForumService } from '../../../services/forum.service';
import { Forum } from '../../../interfaces/forum';
import { MatPaginator } from '@angular/material/paginator';
import { ActivatedRoute, Router } from '@angular/router';
import { formatDateTime, formatDateTimeArray } from '../../../util/formatDate';
import { parseTopPost } from '../../../util/parseTopPost';
import { debounceTime, distinctUntilChanged, fromEvent, map, Subscription } from 'rxjs';
import { AuthService } from '../../../services/auth.service';
import { ForumFormDialogComponent } from './forum-form-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { NotificationService } from '../../../services/notification.service';
import { ForumRequestFormDialogComponent } from './forum-request-form-dialog.component';
import { GameService } from '../../../services/game.service';
import { FormBuilder, FormGroup } from '@angular/forms';
import { forumFilters } from '../../../interfaces/forumFilters';
import { MatSelectChange } from '@angular/material/select';

@Component({
  selector: 'app-forum',
  templateUrl: './forum.component.html'
})
export class ForumComponent extends BaseAdComponent implements AfterViewInit {
  protected subForumList: Forum[] = [];
  protected totalSubforums = 0;
  protected noSubForums = false;
  protected currentForum?: Forum;
  protected path?: any;

  protected filtered = false;

  protected forumId?: number = undefined;
  private routeParamsSubscription?: Subscription;

  protected gameList: any[] = [];
  private filters: forumFilters = {};
  protected filterForm: FormGroup;

  @ViewChild('paginator') private paginator!: MatPaginator;
  @ViewChild('searchInput', { static: false }) private searchInput?: ElementRef;


  protected formatDateTime = formatDateTime;
  protected formatDateTimeArray = formatDateTimeArray;

  constructor(
    protected authService: AuthService,
    private forumService: ForumService,
    private notificationService: NotificationService,
    private gameService: GameService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    protected dialog: MatDialog,
    protected backgroundService: BackgroundService,
    adService: AdService,
    private cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);

    this.filterForm = this.fb.group({
      isDeleted: [null],
      game: [null],
      search: [null]
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.loadGames();

    this.backgroundService.setClasses(['fallingCds']);

    this.routeParamsSubscription = this.route.params.subscribe(params => {
      if (params['id']) {
        this.forumId = params['id'];
      } else {
        this.forumId = 1;
      }

      this.loadForum(this.forumId);

      if (this.forumId) {
        this.loadPath(this.forumId);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.routeParamsSubscription) {
      this.routeParamsSubscription.unsubscribe();
    }
  }

  ngAfterViewInit() {
    this.paginator.page.subscribe(() => {
      this.loadForum(this.forumId);
    });
  }

  activateSearchFilter() {
    setTimeout(() => {
      if (this.searchInput) {
        fromEvent(this.searchInput.nativeElement, 'input').pipe(
          map((event: any) => event.target.value),
          debounceTime(300),
          distinctUntilChanged()
        ).subscribe(value => {
          this.onSearchFilterChange(value);
        });
      }
    }, 0);
  }

  loadGames() {
    this.gameService.getGames().subscribe({
      next: (response: any) => {
        this.gameList = response.content;
      }
    });
  }

  loadPath(id: number) {
    this.path = undefined;
    this.forumService.getForumPath(id).subscribe({
      next: (response: any) => {
        if (response) {
          this.path = response.reverse();
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  loadForum(id?: number) {
    this.currentForum = undefined;
    this.subForumList = [];
    this.noSubForums = true;
    this.totalSubforums = 0;
    /*
    this.path = undefined;
    */

    const page = this.paginator ? this.paginator.pageIndex + 1 : 1;
    const size = this.paginator ? this.paginator.pageSize : 10;

    this.forumService.getForum(id, page, size, this.filters).subscribe({
      next: (response: any) => {
        if (response && response.content.length > 0) {
          this.currentForum = response.content[0];

          const subforums = response.content.slice(1);
          this.subForumList = subforums;
          this.totalSubforums = response.totalElements - 1;
          this.noSubForums = subforums.length === 0;

          this.subForumList.forEach(subforum => {
            if (subforum.topPost) {
              subforum.lastPost = parseTopPost(subforum.topPost);
            }
          });

          this.activateSearchFilter();
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  openAddSubforumDialog() {
    const dialogRef = this.dialog.open(ForumFormDialogComponent, {
      width: '300px',
      data: {
        parentForumId: this.forumId,
        editing: false
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        this.loadForum(this.forumId);
      }
    });
  }

  openEditSubforumDialog(subForum: Forum) {
    const dialogRef = this.dialog.open(ForumFormDialogComponent, {
      width: '300px',
      data: {
        editing: true,
        parentForumId: this.forumId,
        id: subForum.id,
        name: subForum.forumName,
        description: subForum.description,
        gameTitle: subForum.gameTitle
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        this.loadForum(this.forumId);
      }
    });
  }

  openDeleteSubforumDialog(subForum: Forum) {
    const dialogTitle = 'Forum deletion';
    const dialogContent = 'Are you sure you want to delete the forum "' + subForum.forumName + '"?';
    const submitText = 'Delete';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '340px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        this.deleteSubForum(subForum);
      }
    });
  }

  deleteSubForum(subForum: Forum) {
    if(!subForum.id) {
      console.error('No subforum id found');
      return;
    }

    this.forumService.deleteForum(subForum.id).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Forum deleted successfully');
        this.loadForum(this.forumId);
      },
      error: error => this.notificationService.popErrorToast('Forum deletion failed', error)
    });
  }

  openRestoreSubforumDialog(subForum: Forum) {
    const dialogTitle = 'Forum restoration';
    const dialogContent = 'Are you sure you want to restore the forum "' + subForum.forumName + '"?';
    const submitText = 'Restore';
    const cancelText = 'Cancel';
    const submitColor = 'bg-blue-600 hover:bg-blue-700';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText, submitColor }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        this.restoreSubForum(subForum);
      }
    });
  }

  restoreSubForum(subForum: Forum) {
    if(!subForum.id) {
      console.error('No subforum id found');
      return;
    }

    this.forumService.deleteForum(subForum.id, false).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Forum restored successfully');
        this.loadForum(this.forumId);
      },
      error: error => this.notificationService.popErrorToast('Forum restoration failed', error)
    });
  }

  openAddNewRequestDialog() {
    this.dialog.open(ForumRequestFormDialogComponent, {
      width: '300px',
      data: {
        editing: false,
        parentForum: this.currentForum,
        lockParentForum: true
      }
    });
  }

  routeToRequests() {
    this.router.navigate(['forum-requests']);
  }

  navigateToSubforum(id?: number) {
    this.router.navigate(['forum', id]);
  }

  navigateToLastPost(lastPost: any) {
    this.router.navigate(['forum/' + lastPost.forum_id + '/post/' + lastPost.forum_post_id]);
  }

  navigateToPost(postId: number) {
    this.router.navigate(['forum', this.forumId, 'post', postId]);
  }

  onDeletedFilterChange(event: MatSelectChange) {
    this.filters.isDeleted = event.value;
    this.filtered = true;
    this.loadForum(this.forumId);
  }

  onGameFilterChange(event: MatSelectChange) {
    if (event.value) {
      this.filters.gameId = event.value.id;
      this.filtered = true;
    } else {
      this.filters.gameId = undefined;
    }
    this.loadForum(this.forumId);
  }

  onSearchFilterChange(value: string) {
    this.filters.search = value;
    this.filtered = true;
    this.loadForum(this.forumId);
  }

  clearFilters() {
    this.filters = {};
    this.filterForm.reset();
    this.filterForm.get('userScore')?.get('min')?.setValue(1);
    this.filterForm.get('userScore')?.get('max')?.setValue(10);
  }
}
