import { ChangeDetectorRef, Component, Input, ViewChild, OnInit } from '@angular/core';
import { BackgroundService } from '../../../services/background.service';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { MatPaginator } from '@angular/material/paginator';
import { ActivatedRoute } from '@angular/router';
import { ForumPostService } from '../../../services/forumPost.service';
import { ForumPost } from '../../../models/forumPost';
import { ForumCommentService } from '../../../services/forumComment.service';
import { formatDateTimeArray } from '../../../util/formatDate';
import { ForumService } from '../../../services/forum.service';
import { AuthService } from '../../../services/auth.service';
import { NotificationService } from '../../../services/notification.service';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { ForumCommentEditDialogComponent } from './forum-comment-edit-dialog.component';
import { NotificationAction } from '../../../enums/notificationActions';
import { ForumPostFormDialogComponent } from './forum-post-form-dialog.component';
import { WebsiteUser } from '../../../models/websiteUser';
import { ImageCacheService } from '../../../services/imageCache.service';
import { UserService } from '../../../services/user.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ForumComment } from '../../../models/forumComment';
import { HttpErrorResponse } from '@angular/common/http';
import { Forum } from '../../../models/forum';

@Component({
  selector: 'app-forum-post',
  templateUrl: './forum-post.component.html'
})
export class ForumPostComponent extends BaseAdComponent implements OnInit {
  @ViewChild('paginator') protected paginator!: MatPaginator;
  protected formatDateTimeArray = formatDateTimeArray;

  @Input() protected post?: ForumPost;

  protected commentsList: ForumComment[] = [];
  protected totalComments = 0;
  protected path?: Forum[];
  protected moderators: WebsiteUser[] = [];
  protected imageUrl?: string;
  protected authorProfilePic?: string;

  constructor(
    protected authService: AuthService,
    private forumService: ForumService,
    private forumPostService: ForumPostService,
    private forumCommentService: ForumCommentService,
    private notificationService: NotificationService,
    private imageCacheService: ImageCacheService,
    private userService: UserService,
    private route: ActivatedRoute,
    protected dialog: MatDialog,
    private sanitizer: DomSanitizer,
    protected backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.backgroundService.setClasses(['fallingCds']);
    
    this.route.params.subscribe(params => {
      if (params['postid']) {
        this.loadPost(params['postid']);
        this.loadComments(params['postid']);
      }
      if (params['forumid']) {
        this.loadPath(params['forumid']);
        this.loadModerators(params['forumid']);
      }
    });
  }

  loadPath(id: number) {
    this.path = undefined;

    this.forumService.getForumPath(id).subscribe({
      next: response => {
        if (response) {
          this.path = response;
          this.path = this.path.reverse();
          
          if (this.post) {
            this.path.push({ id: this.post.id, forumName: this.post.title });
          }
        }
      },
      error: (error: HttpErrorResponse) => this.notificationService.popErrorToast('Failed to load forum path', error)
    });
  }

  loadPost(id: number) {
    this.post = undefined;

    this.forumPostService.getPost(id).subscribe({
      next: response => {
        if (response && response.content.length > 0 && response.id && response.picture && response.author && response.author.nickname && response.author.profilepic) {
          this.post = response;

          this.loadPostPicture(response.id, response.picture);
          this.loadUserProfilePicture(response.author.nickname, response.author.profilepic);

          // Potential fix for the issue where the path is not properly loaded when the component is initialized
          if (this.route.snapshot.params['forumid']) {
            this.loadPath(this.route.snapshot.params['forumid']);
          }
        }
      },
      error: (error: HttpErrorResponse) => this.notificationService.popErrorToast('Failed to load post', error)
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

  loadComments(id: number) {
    this.commentsList = [];

    let page = 1;
    let size = 10;

    if (this.paginator) {
      page = this.paginator.pageIndex + 1;
      size = this.paginator.pageSize;
    }

    this.forumCommentService.getComments(id, page, size).subscribe({
      next: response => {
        if (response && response.content.length > 0) {
          this.commentsList = response.content;

          // Load profile pics for all comments
          this.commentsList.forEach((comment: ForumComment) => {
            if (comment.author.nickname != this.post?.author?.nickname) {
              this.loadUserProfilePicture(comment.author.nickname!, comment.author.profilepic!);
            }
          });
        }
      },
      error: (error: HttpErrorResponse) => this.notificationService.popErrorToast('Failed to load comments', error)
    });
  }

  getTrustedContent(html: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }

  loadPostPicture(postId: number, pictureUrl: string) {
    const didPostPicChange = this.imageCacheService.didPictureNameChange("postPicName" + postId, pictureUrl);

    if (!didPostPicChange && this.imageCacheService.isCached("postPic" + postId)) {
      const cachedImage = this.imageCacheService.getCachedImage("postPic" + postId);
      if (cachedImage) {
        this.imageUrl = cachedImage;
      }

    } else {
      this.forumPostService.getPicture(postId).subscribe({
        next: response2 => {
          if (response2) {
            this.imageUrl = URL.createObjectURL(response2);
            this.imageCacheService.cacheBlob("postPic" + postId, response2);
            this.imageCacheService.cacheProfilePicName("postPicName" + postId, pictureUrl);
          }
        },
        error: error => {
          console.error(error);
        }
      });
    }
  }

  loadUserProfilePicture(nickName: string, pictureUrl: string) {
    const didProfilePicChange = this.imageCacheService.didPictureNameChange("profilePicName" + nickName, pictureUrl);

    if (!didProfilePicChange && this.imageCacheService.isCached("profilePic" + nickName)) {
      const cachedImage = this.imageCacheService.getCachedImage("profilePic" + nickName);
      if (cachedImage) {
        if (this.post && this.post.author && this.post.author.nickname == nickName) {
          this.authorProfilePic = cachedImage;

        } else {
          this.commentsList.forEach((comment: ForumComment) => {
            if (comment.author.nickname == nickName) {
              comment.author.picture = cachedImage;
            }
          });
        }
      }

    } else {
      this.userService.getProfilePicture(nickName).subscribe({
        next: response2 => {
          if (response2) {
            if (this.post && this.post.author && this.post.author.nickname == nickName) {
              this.authorProfilePic = URL.createObjectURL(response2);
            } else {
              this.commentsList.forEach((comment: ForumComment) => {
                if (comment.author.nickname == nickName) {
                  comment.author.picture = URL.createObjectURL(response2);
                }
              });
            }
            this.imageCacheService.cacheBlob("profilePic" + nickName, response2);
            this.imageCacheService.cacheProfilePicName("profilePicName" + nickName, pictureUrl);
          }
        },
        error: error => {
          console.error(error);
        }
      });
    }
  }

  canEditComment(comment: ForumComment) {
    return (this.authService.isAuthenticated() && comment.author.nickname === this.authService.getNickname());
  }

  canDeleteComment(comment: ForumComment) {
    return this.authService.isAuthenticated()
    && (comment.author.nickname === this.authService.getNickname()
    || this.moderators.some(moderator => moderator.nickname === this.authService.getNickname())
    || this.authService.isAdmin());
  }

  openEditCommentDialog(comment: ForumComment) {
    const dialogRef = this.dialog.open(ForumCommentEditDialogComponent, {
      data: {
        commentId: comment.id,
        commentContent: comment.content
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.loadComments(this.post?.id || 0);
      }
    });
  }

  openDeleteCommentDialog(comment: ForumComment) {
    const dialogTitle = 'Deleting comment';
    const dialogContent = 'Are you sure you want to delete this comment'+ (comment.author?.nickname ? ` by ${comment.author?.nickname}` : '') + '?';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteComment(comment.id);
      }
    });
  }

  deleteComment(id: number) {
    this.forumCommentService.deleteComment(id).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Comment deleted successfully');
        this.commentsList = this.commentsList.filter((comment: ForumComment) => comment.id !== id);
      },
      error: error => this.notificationService.popErrorToast('Comment deletion failed', error)
    });
  }

  canManagePost(post: ForumPost) {
    return this.authService.isAuthenticated() &&
    (post.author?.nickname === this.authService.getNickname()
    || this.moderators.some(moderator => moderator.nickname === post.author?.nickname)
    || this.authService.isAdmin());
  }

  openDeletePostDialog(post: ForumPost) {
    const dialogTitle = 'Deleting post';
    const dialogContent = 'Are you sure you want to delete this post?';

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

  openEditPostDialog(post: ForumPost) {
    const dialogRef = this.dialog.open(ForumPostFormDialogComponent, {
      data: {
        forumId: post.forum.id,
        editing: true,
        content: post.content,
        title: post.title,
        id: post.id
      }
    });

    dialogRef.afterClosed().subscribe((response) => {
      if (response && post.id) {
        this.loadPost(post.id);
      }
    });
  }

  deletePost(id: number) {
    this.forumPostService.deletePost(id).subscribe({
      next: () => { this.notificationService.popSuccessToast('Post deleted successfully', NotificationAction.GO_BACK); },
      error: error => this.notificationService.popErrorToast('Post deletion failed', error)
    });
  }

  reloadComments() {
    if (this.post?.id) {
      this.loadComments(this.post.id);
    }
  }
}
