import { AfterViewInit, Component, Input, SimpleChanges, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { ForumPostService } from '../../../services/forumPost.service';
import { formatDateTimeArray } from '../../../util/formatDate';
import { ForumPost } from '../../../interfaces/forumPost';
import { ForumPostFormDialogComponent } from './forum-post-form-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { NotificationService } from '../../../services/notification.service';
import { AuthService } from '../../../services/auth.service';
import { ForumService } from '../../../services/forum.service';
import { WebsiteUser } from '../../../interfaces/websiteUser';
import { ImageCacheService } from '../../../services/imageCache.service';
import { Observer } from 'rxjs';

@Component({
  selector: 'app-forum-post-list',
  templateUrl: './forum-post-list.component.html'
})
export class ForumPostListComponent implements AfterViewInit {
  @Input() public currentForumId?: number;
  protected postList: ForumPost[] = [];
  protected totalPosts: number = 0;
  @ViewChild('paginator') protected paginator!: MatPaginator;
  protected formatDateTimeArray = formatDateTimeArray;
  protected moderators: WebsiteUser[] = [];

  constructor(
    private forumPostService: ForumPostService,
    private notificationService: NotificationService,
    private imageCacheService: ImageCacheService,
    private forumService: ForumService,
    protected dialog: MatDialog,
    protected authService: AuthService,
    private router: Router
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentForumId'] && changes['currentForumId'].currentValue) {
      this.loadPosts(changes['currentForumId'].currentValue);
      this.loadModerators(changes['currentForumId'].currentValue);
    }
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

    this.forumPostService.getPosts(id, page, size).subscribe({
      next: (response: any) => {
        if (response && response.content.length > 0) {
          this.postList = response.content;
          this.totalPosts = response.totalElements;
          this.loadPostPictures();
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  loadModerators(forumId: number) {
    this.forumService.getModerators(forumId).subscribe({
      next: (response: any) => {
        if (response && response.length > 0) {
          this.moderators = response;
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  openNewPostDialog() {
    const dialogRef = this.dialog.open(ForumPostFormDialogComponent, {
      width: '400px',
      data: {
        forumId: this.currentForumId,
        editing: false
      }
    });

    dialogRef.afterClosed().subscribe((response) => {
      console.log(response, "AWAWAW!!!");
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
      width: '400px',
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
          const observerPicture: Observer<any> = {
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
            error: error => {
              console.error(error);
            },
            complete: () => {}
          };
          this.forumPostService.getPicture(post.id).subscribe(observerPicture);
        }
      }
    });
  }

  navigateToPost(id: number | undefined) {
    if (id) {
      this.router.navigate([`forum/${this.currentForumId ?? 0}/post/${id}`]);
    }
  }
}
