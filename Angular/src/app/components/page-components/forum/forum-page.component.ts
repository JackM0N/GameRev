import { AfterViewInit, ChangeDetectorRef, Component, Input, ViewChild } from '@angular/core';
import { BackgroundService } from '../../../services/background.service';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { ForumService } from '../../../services/forum.service';
import { Forum } from '../../../interfaces/forum';
import { MatPaginator } from '@angular/material/paginator';
import { ActivatedRoute, Router } from '@angular/router';
import { ForumPostService } from '../../../services/forumPost.service';

@Component({
  selector: 'app-forum-page',
  templateUrl: './forum-page.component.html'
})
export class ForumPageComponent extends BaseAdComponent implements AfterViewInit {
  @Input() currentForum?: Forum;
  private forumId: number = 2;
  public postList: any[] = [];
  public totalPosts: number = 0;
  @ViewChild('paginator') paginator!: MatPaginator;

  constructor(
    private forumService: ForumService,
    private forumPostService: ForumPostService,
    private router: Router,
    private route: ActivatedRoute,
    backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
  }

  ngOnInit(): void {
    if (this.currentForum) {
      this.loadPosts(this.currentForum.id);
    }
  }

  override ngAfterViewInit() {
    super.ngAfterViewInit();
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
          this.currentForum = response.content[0];
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  navigateToSubforum(id: number) {
    this.router.navigate(['forum', id]);
  }
}
