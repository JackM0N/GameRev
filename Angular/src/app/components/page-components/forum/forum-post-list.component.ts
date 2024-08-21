import { AfterViewInit, ChangeDetectorRef, Component, Input, ViewChild } from '@angular/core';
import { BackgroundService } from '../../../services/background.service';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { Forum } from '../../../interfaces/forum';
import { MatPaginator } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { ForumPostService } from '../../../services/forumPost.service';
import { ForumService } from '../../../services/forum.service';

@Component({
  selector: 'app-forum-post-list',
  templateUrl: './forum-post-list.component.html',
  styleUrls: ['./forum-post-list.component.css']
})
export class ForumPostListComponent extends BaseAdComponent implements AfterViewInit {
  @Input() currentForum?: Forum;
  public postList: any[] = [];
  public totalPosts: number = 0;
  public path?: any;
  @ViewChild('paginator') paginator!: MatPaginator;

  constructor(
    private forumService: ForumService,
    private forumPostService: ForumPostService,
    private router: Router,
    backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
  }

  ngOnInit(): void {
    if (this.currentForum) {
      this.loadPosts(this.currentForum.id);
      this.loadPath(this.currentForum.id);
    }
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
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  loadPosts(id: number) {
    this.postList = [];

    var page = 1;
    var size = 10;

    if (this.paginator) {
      page = this.paginator.pageIndex + 1;
      size = this.paginator.pageSize;
    }

    this.forumPostService.getPosts(id, page, size).subscribe({
      next: (response: any) => {
        console.log(response);
        if (response && response.content.length > 0) {
          this.postList = response.content;
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  navigateToPost(id: number) {
    this.router.navigate([`forum/${this.currentForum?.id ?? 0}/post/${id}`]);
  }
}
