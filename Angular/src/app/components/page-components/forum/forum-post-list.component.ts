import { AfterViewInit, Component, Input, SimpleChanges, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { ForumPostService } from '../../../services/forumPost.service';
import { formatDateTimeArray } from '../../../util/formatDate';
import { ForumPost } from '../../../interfaces/forumPost';

@Component({
  selector: 'app-forum-post-list',
  templateUrl: './forum-post-list.component.html'
})
export class ForumPostListComponent implements AfterViewInit {
  @Input() currentForumId?: number;
  public postList: ForumPost[] = [];
  public totalPosts: number = 0;
  @ViewChild('paginator') paginator!: MatPaginator;
  public formatDateTimeArray = formatDateTimeArray;

  constructor(
    private forumPostService: ForumPostService,
    private router: Router
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentForumId'] && changes['currentForumId'].currentValue) {
      this.loadPosts(changes['currentForumId'].currentValue);
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
        }
      },
      error: (error: any) => console.error(error)
    });
  }

  openNewPostDialog() {
    
  }

  navigateToPost(id: number) {
    this.router.navigate([`forum/${this.currentForumId ?? 0}/post/${id}`]);
  }
}
