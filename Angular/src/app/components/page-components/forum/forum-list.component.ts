import { AfterViewInit, ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { BackgroundService } from '../../../services/background.service';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { ForumService } from '../../../services/forum.service';
import { Forum } from '../../../interfaces/forum';
import { MatPaginator } from '@angular/material/paginator';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-forum-list',
  templateUrl: './forum-list.component.html',
  styleUrls: ['./forum-list.component.css']
})
export class ForumListComponent extends BaseAdComponent implements AfterViewInit {
  public subForumList: Forum[] = [];
  public totalSubforums = 0;
  public noSubForums = false;
  public currentForum?: Forum;
  private forumId?: number;
  public isSingleForum: boolean = false;
  public path?: any;
  @ViewChild('paginator') paginator!: MatPaginator;

  constructor(
    private forumService: ForumService,
    private router: Router,
    private route: ActivatedRoute,
    backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.forumId = +params['id'];
        this.loadForum(this.forumId);
        if (this.forumId) {
          this.loadPath(this.forumId);
        }
      }
    });
  }

  override ngAfterViewInit() {
    super.ngAfterViewInit();
    /*
    this.loadForum(this.forumId);
    if (this.forumId) {
      this.loadPath(this.forumId);
    }
    */
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

  loadForum(id?: number) {
    this.currentForum = undefined;
    this.subForumList = [];
    this.noSubForums = false;
    this.totalSubforums = 0;
    this.isSingleForum = false;
    this.path = undefined;

    var page = 1;
    var size = 10;

    if (this.paginator) {
      page = this.paginator.pageIndex + 1;
      size = this.paginator.pageSize;
    }

    this.forumService.getForum(id, page, size).subscribe({
      next: (response: any) => {
        console.log(response);
        if (response && response.content.length > 0) {
          // Separate the first item as the main forum
          this.currentForum = response.content[0];
          
          // Set the remaining items as the subforums
          const subforums = response.content.slice(1);
          this.subForumList = subforums;
          this.totalSubforums = response.totalElements - 1;
          this.noSubForums = subforums.length == 0;

          this.isSingleForum = (this.totalSubforums == 0);
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  navigateToSubforum(id: number) {
    this.router.navigate(['forum', id]);
  }
}
