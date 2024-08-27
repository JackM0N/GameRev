import { AfterViewInit, ChangeDetectorRef, Component, Input, ViewChild } from '@angular/core';
import { BackgroundService } from '../../../services/background.service';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { MatPaginator } from '@angular/material/paginator';
import { ActivatedRoute } from '@angular/router';
import { ForumPostService } from '../../../services/forumPost.service';
import { ForumPost } from '../../../interfaces/forumPost';
import { ForumCommentService } from '../../../services/forumComment.service';
import { formatDateTimeArray } from '../../../util/formatDate';
import { ForumService } from '../../../services/forum.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { NotificationService } from '../../../services/notification.service';
import { ForumComment } from '../../../interfaces/forumComment';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ForumCommentEditDialogComponent } from './forum-comment-edit-dialog.component';
import { trimmedValidator } from '../../../validators/trimmedValidator';

@Component({
  selector: 'app-forum-post',
  templateUrl: './forum-post.component.html'
})
export class ForumPostComponent extends BaseAdComponent implements AfterViewInit {
  @Input() post?: ForumPost;
  @ViewChild('paginator') paginator!: MatPaginator;
  public formatDateTimeArray = formatDateTimeArray;

  public commentsList: any[] = [];
  public totalComments: number = 0;
  public path?: any;
  public commentForm: FormGroup;
  public minLength: number = 4;

  constructor(
    public authService: AuthService,
    private forumService: ForumService,
    private forumPostService: ForumPostService,
    private forumCommentService: ForumCommentService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    private fb: FormBuilder,
    public dialog: MatDialog,
    backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
    this.commentForm = this.fb.group({
      content: [{value: '', disabled: !authService.isAuthenticated()}, [Validators.required, Validators.minLength(this.minLength), trimmedValidator(this.minLength)]]
    });
  }

  override ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['postid']) {
        this.loadPost(params['postid']);
        this.loadComments(params['postid']);
      }
      if (params['forumid']) {
        this.loadPath(params['forumid']);
      }
    });
  }

  override ngAfterViewInit() {
    super.ngAfterViewInit();
  }

  loadPath(id: number) {
    this.path = undefined;

    this.forumService.getForumPath(id).subscribe({
      next: (response: any) => {
        if (response) {
          this.path = response;
          this.path = this.path.reverse();
          this.path.push({ id: this.post?.id, forumName: this.post?.title });
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  loadPost(id: number) {
    this.post = undefined;

    this.forumPostService.getPost(id).subscribe({
      next: (response: any) => {
        if (response && response.content.length > 0) {
          this.post = response;
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  loadComments(id: number) {
    this.commentsList = [];

    var page = 1;
    var size = 10;

    if (this.paginator) {
      page = this.paginator.pageIndex + 1;
      size = this.paginator.pageSize;
    }

    this.forumCommentService.getComments(id, page, size).subscribe({
      next: (response: any) => {
        if (response && response.content.length > 0) {
          this.commentsList = response.content;
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  submitComment() {
    if (this.commentForm.valid) {
      const newComment = {
        content: this.commentForm.value.content,
        forumPostId: this.post?.id
      };

      this.forumCommentService.addComment(newComment).subscribe({
        next: () => {
          this.notificationService.popSuccessToast('Comment posted successfully', false);
          window.location.reload();
        },
        error: error => this.notificationService.popErrorToast('Comment posting failed', error)
      });
    }
  }

  canEditComment(comment: ForumComment) {
    return (this.authService.isAuthenticated() && comment.author.nickname === this.authService.getNickname());
  }

  canDeleteComment(comment: ForumComment) {
    return this.authService.isAuthenticated() && (comment.author.nickname === this.authService.getNickname() || this.authService.isAdmin());
  }

  openEditCommentDialog(comment: ForumComment) {
    const dialogRef = this.dialog.open(ForumCommentEditDialogComponent, {
      width: '300px',
      data: { commentContent: comment.content }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.editComment(comment, dialogRef);
      }
    });
  }

  editComment(comment: ForumComment, dialogRef: MatDialogRef<ForumCommentEditDialogComponent>) {
    
  }

  openDeleteDialog(comment: ForumComment) {
    const dialogTitle = 'Deleting comment';
    const dialogContent = 'Are you sure you want to delete this comment?';

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
        this.notificationService.popSuccessToast('Comment deleted successfully', false);
        this.commentsList = this.commentsList.filter((comment: any) => comment.id !== id);
      },
      error: error => this.notificationService.popErrorToast('Comment deletion failed', error)
    });
  }
}
