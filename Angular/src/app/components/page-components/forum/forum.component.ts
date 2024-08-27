import { AfterViewInit, ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { BackgroundService } from '../../../services/background.service';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { ForumService } from '../../../services/forum.service';
import { Forum } from '../../../interfaces/forum';
import { MatPaginator } from '@angular/material/paginator';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { formatDateTime, formatDateTimeArray } from '../../../util/formatDate';
import { parseTopPost } from '../../../util/parseTopPost';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../services/auth.service';
import { ForumFormDialogComponent } from './forum-form-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-forum',
  templateUrl: './forum.component.html'
})
export class ForumComponent extends BaseAdComponent implements AfterViewInit {
  public subForumList: Forum[] = [];
  public totalSubforums = 0;
  public noSubForums = false;
  public currentForum?: Forum;
  public path?: any;

  public forumId?: number;
  private routeParamsSubscription?: Subscription;

  @ViewChild('paginator') paginator!: MatPaginator;
  @ViewChild('paginatorPosts') paginatorPosts!: MatPaginator;

  public formatDateTime = formatDateTime;
  public formatDateTimeArray = formatDateTimeArray;

  constructor(
    public authService: AuthService,
    private forumService: ForumService,
    private router: Router,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.routeParamsSubscription = this.route.params.subscribe(params => {
      this.forumId = params['id'];
      this.loadForum(this.forumId);
      if (this.forumId) {
        this.loadPath(this.forumId);
      }
    });

    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this._adService.adBoxActive$.subscribe(isActive => {
          super.adjustMargin(isActive);
        });
      }
    });
  }

  override ngOnDestroy(): void {
    if (this.routeParamsSubscription) {
      this.routeParamsSubscription.unsubscribe();
    }
    super.ngOnDestroy();
  }

  override ngAfterViewInit() {
    super.ngAfterViewInit();

    this.paginator.page.subscribe(() => {
      this.loadForum(this.forumId);
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
    this.noSubForums = false;
    this.totalSubforums = 0;
    this.path = undefined;

    const page = this.paginator ? this.paginator.pageIndex + 1 : 1;
    const size = this.paginator ? this.paginator.pageSize : 10;

    this.forumService.getForum(id, page, size).subscribe({
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
  }

  openEditSubforumDialog(subForum: Forum) {
    const dialogRef = this.dialog.open(ForumFormDialogComponent, {
      width: '300px',
      data: {
        editing: true,
        id: subForum.id,
        name: subForum.forumName,
        description: subForum.description,
        gameTitle: subForum.gameTitle
      }
    });
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
}
