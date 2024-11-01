import { AfterViewInit, Component, ElementRef, HostListener, Input, SimpleChanges, ViewChild, OnChanges, OnInit } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { ForumPostService } from '../../../services/forumPost.service';
import { formatDateTimeArray } from '../../../util/formatDate';
import { ForumPost } from '../../../models/forumPost';
import { ForumPostFormDialogComponent } from './forum-post-form-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { NotificationService } from '../../../services/notification.service';
import { AuthService } from '../../../services/auth.service';
import { ForumService } from '../../../services/forum.service';
import { WebsiteUser } from '../../../models/websiteUser';
import { ImageCacheService } from '../../../services/imageCache.service';
import { debounceTime, distinctUntilChanged, fromEvent, map } from 'rxjs';
import { forumPostFilters } from '../../../filters/forumPostFilters';
import { FormBuilder, FormGroup } from '@angular/forms';
import { AdService } from '../../../services/ad.service';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-forum-post-list',
  templateUrl: './forum-post-list.component.html'
})
export class ForumPostListComponent implements AfterViewInit, OnChanges, OnInit {
  @Input() public currentForumId?: number;
  protected postList: ForumPost[] = [];
  protected totalPosts = 0;
  @ViewChild('paginator') protected paginator!: MatPaginator;
  protected formatDateTimeArray = formatDateTimeArray;
  protected moderators: WebsiteUser[] = [];

  @ViewChild('searchInput', { static: false }) private searchInput?: ElementRef;
  protected filtered = false;
  private filters: forumPostFilters = {};
  protected filterForm: FormGroup;

  constructor(
    private forumPostService: ForumPostService,
    private notificationService: NotificationService,
    private imageCacheService: ImageCacheService,
    private forumService: ForumService,
    protected dialog: MatDialog,
    protected authService: AuthService,
    private fb: FormBuilder,
    private elRef: ElementRef,
    private router: Router,
    private adService: AdService,
    private datePipe: DatePipe,
  ) {
    this.filterForm = this.fb.group({
      dateRange: this.fb.group({
        start: [null],
        end: [null]
      }),
      search: [null]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentForumId'] && changes['currentForumId'].currentValue) {
      this.loadPosts(changes['currentForumId'].currentValue);
      this.loadModerators(changes['currentForumId'].currentValue);
    }
  }

  ngOnInit(): void {
    this.adService.adBoxActive$.subscribe(() => {
      setTimeout(() => {
        this.adjustFilterVisibility();
      }, 0);
    });
  }

  ngAfterViewInit(): void {
    this.paginator.page.subscribe(() => {
      if (this.currentForumId) {
        this.loadPosts(this.currentForumId);
      }
    });
  }

  loadPosts(id: number) {
    this.postList = [];
    this.totalPosts = 0;

    const page = this.paginator ? this.paginator.pageIndex + 1 : 1;
    const size = this.paginator ? this.paginator.pageSize : 10;

    this.forumPostService.getPosts(id, page, size, "postDate", "desc", this.filters).subscribe({
      next: response => {
        if (response && response.content.length > 0) {
          this.postList = response.content;
          this.totalPosts = response.totalElements;
          this.loadPostPictures();

          setTimeout(() => {
            this.adjustFilterVisibility();
          }, 0);

          this.activateSearchFilter();
        }
      },
      error: (error: HttpErrorResponse) => this.notificationService.popErrorToast('Failed to load posts', error)
    });
  }

  loadModerators(forumId: number) {
    this.forumService.getModerators(forumId).subscribe({
      next: response => {
        if (response && response.length > 0) {
          this.moderators = response;
        }
      },
      error: (error: HttpErrorResponse) => this.notificationService.popErrorToast('Failed to load moderator info', error)
    });
  }

  openNewPostDialog() {
    const dialogRef = this.dialog.open(ForumPostFormDialogComponent, {
      data: {
        forumId: this.currentForumId,
        editing: false
      }
    });

    dialogRef.afterClosed().subscribe((response) => {
      if (response) {
        this.loadPosts(this.currentForumId ?? 0);
      }
    });
  }

  canManagePost(post: ForumPost) {
    return this.authService.isAuthenticated() &&
    (post.author?.nickname === this.authService.getNickname()
    || this.authService.isAdmin());
  }

  isModerator() {
    return this.moderators.some(moderator => moderator.nickname === this.authService.getNickname());
  }

  canManageAnyPost() {
    return this.authService.isAuthenticated() &&
    (this.postList.some(post => post.author?.nickname === this.authService.getNickname())
    || this.isModerator()
    || this.authService.isAdmin());
  }

  openEditPostDialog(post: ForumPost) {
    const dialogRef = this.dialog.open(ForumPostFormDialogComponent, {
      data: {
        forumId: this.currentForumId,
        editing: true,
        content: post.content,
        title: post.title,
        id: post.id,
        picture: post.picture
      }
    });

    dialogRef.afterClosed().subscribe((response) => {
      if (response) {
        this.loadPosts(this.currentForumId ?? 0);
      }
    });
  }

  openDeletePostDialog(post: ForumPost | undefined) {
    if (!post) {
      console.error('No post to delete');
      return;
    }

    const dialogTitle = 'Deleting comment';
    const dialogContent = 'Are you sure you want to delete this comment'+ (post.author?.nickname ? ` by ${post.author?.nickname}` : '') + '?';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true && post.id) {
        this.deletePost(post.id);
      }
    });
  }

  deletePost(id: number) {
    this.forumPostService.deletePost(id).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Post deleted successfully');
        this.loadPosts(this.currentForumId ?? 0);
      },
      error: error => this.notificationService.popErrorToast('Post deletion failed', error)
    });
  }

  loadPostPictures() {
    this.postList.forEach(post => {
      if (post.picture && post.id) {

        const didPicChange = this.imageCacheService.didPictureNameChange("postPicName" + post.id, post.picture);
        if (!didPicChange && this.imageCacheService.isCached("postPic" + post.id)) {
          const cachedImage = this.imageCacheService.getCachedImage("postPic" + post.id);
          if (cachedImage) {
            post.picture = cachedImage;
          }
    
        } else {
          this.forumPostService.getPicture(post.id).subscribe({
            next: response2 => {
              if (response2) {
                this.imageCacheService.cacheBlob("postPic" + post.id, response2);
                if (post.picture) {
                  this.imageCacheService.cacheProfilePicName("postPicName" + post.id, post.picture);
                }
                if (post) {
                  post.picture = URL.createObjectURL(response2);
                }
              }
            },
            error: error => { console.error(error); }
          });
        }
      }
    });
  }

  navigateToPost(id: number | undefined) {
    if (id) {
      this.router.navigate([`forum/${this.currentForumId ?? 0}/post/${id}`]);
    }
  }

  // Filters

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.adjustFilterVisibility();
  }

  protected hideFilters = false;
  protected isFilterExpanded = false;
  toggleFilterPanel() {
    this.isFilterExpanded = !this.isFilterExpanded;
  }

  private isForumContentSmall(): boolean {
    const subForumContent = this.elRef.nativeElement.querySelector('#forum-posts');
    const filterForm = this.elRef.nativeElement.querySelector('#posts-filter-form');
    const filterMenuButton = this.elRef.nativeElement.querySelector('#posts-filters-menu-button');

    if (subForumContent && filterForm && filterMenuButton) {
      const subForumContentWidth = subForumContent.offsetWidth;
      
      return (subForumContentWidth < 960);
    }

    return false;
  }

  @HostListener('document:click', ['$event'])
  onClick(event: MouseEvent) {
    const targetElement = event.target as HTMLElement;
    const filterForm = this.elRef.nativeElement.querySelector('#posts-filter-form');
    const filterMenuButton = this.elRef.nativeElement.querySelector('#posts-filters-menu-button');

    if (filterForm && this.isFilterExpanded && !filterForm.contains(targetElement) && (filterMenuButton && !filterMenuButton.contains(targetElement))) {
      this.isFilterExpanded = false;
    }
  }

  adjustFilterVisibility() {
    const isSmall = this.isForumContentSmall();

    this.hideFilters = isSmall;

    if (!isSmall && this.isFilterExpanded) {
      this.isFilterExpanded = false;
    }
  }

  activateSearchFilter() {
    setTimeout(() => {
      if (this.searchInput) {
        fromEvent<InputEvent>(this.searchInput.nativeElement, 'input').pipe(
          map((event) => (event.target as HTMLInputElement).value),
          debounceTime(300),
          distinctUntilChanged()

        ).subscribe(value => {
          this.onSearchFilterChange(value);
        });
      }
    }, 0);
  }

  onSearchFilterChange(value: string) {
    this.filters.search = value;
    this.filtered = true;
    this.loadPosts(this.currentForumId ?? 0);
  }

  onStartDateChange(event: MatDatepickerInputEvent<Date>) {
    const selectedDate = event.value;

    if (selectedDate) {
      const formattedDate = this.datePipe.transform(selectedDate, 'yyyy-MM-dd');
      
      if (formattedDate) {
        this.filters.startDate = formattedDate;
      }

      if (this.filters.startDate && this.filters.endDate) {
        this.loadPosts(this.currentForumId ?? 0);
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
        this.loadPosts(this.currentForumId ?? 0);
      }
    }
  }

  clearFilters() {
    this.filters = {};
    this.filterForm.reset();
    this.loadPosts(this.currentForumId ?? 0);
  }
}
