import { AfterViewInit, ChangeDetectorRef, Component, Input, ViewChild } from '@angular/core';
import { BackgroundService } from '../../../services/background.service';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { MatPaginator } from '@angular/material/paginator';
import { ActivatedRoute } from '@angular/router';
import { ForumPostService } from '../../../services/forumPost.service';
import { ForumPost } from '../../../interfaces/forumPost';
import { ForumCommentService } from '../../../services/forumComment.service';
import { formatDateTime } from '../../../util/formatDate';
import { ForumService } from '../../../services/forum.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-forum-post',
  templateUrl: './forum-post.component.html',
  styleUrls: ['./forum-post.component.css']
})
export class ForumPostComponent extends BaseAdComponent implements AfterViewInit {
  @Input() post?: ForumPost;
  @ViewChild('paginator') paginator!: MatPaginator;
  public formatDateTime = formatDateTime;

  public commentsList: any[] = [];
  public totalComments: number = 0;
  public path?: any;
  public commentForm: FormGroup;

  constructor(
    public authService: AuthService,
    private forumService: ForumService,
    private forumPostService: ForumPostService,
    private forumCommentService: ForumCommentService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    private fb: FormBuilder,
    backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
    this.commentForm = this.fb.group({
      content: [{value: '', disabled: !authService.isAuthenticated()}, [Validators.required, Validators.minLength(5)]]
    });
  }

  ngOnInit(): void {
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
      const token = this.authService.getToken();

      if (token === null) {
        console.error('Token is null');
        return;
      }

      const newComment = {
        content: this.commentForm.value.content,
        forumPostId: this.post?.id
      };

      //this.commentsList.push(newComment);
      //this.commentForm.reset();

      this.forumCommentService.addComment(token, newComment).subscribe({
        next: () => { this.notificationService.popSuccessToast('Comment posted successfully', false); },
        error: error => this.notificationService.popErrorToast('Comment posting failed', error)
      });
    }
  }
}
