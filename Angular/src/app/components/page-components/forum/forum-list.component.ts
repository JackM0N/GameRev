import { AfterViewInit, ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { BackgroundService } from '../../../services/background.service';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';
import { ForumService } from '../../../services/forum.service';
import { Forum } from '../../../interfaces/forum';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-forum-list',
  templateUrl: './forum-list.component.html'
})
export class ForumListComponent extends BaseAdComponent implements AfterViewInit {
  public dataSource: MatTableDataSource<Forum> = new MatTableDataSource<Forum>([]);
  public totalSubforums = 0;
  public noSubForums = false;
  @ViewChild('paginator') paginator!: MatPaginator;

  constructor(
    private forumService: ForumService,
    backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
  }

  ngOnInit(): void {
  }

  override ngAfterViewInit() {
    super.ngAfterViewInit();
    
    this.loadForum();
  }

  loadForum() {
    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;

    this.forumService.getForum(2, page, size).subscribe({
      next: (response: any) => {
        console.log(response);
        if (response) {
          this.dataSource = new MatTableDataSource<Forum>(response.content);
          this.totalSubforums = response.totalElements;
          this.noSubForums = response.content.length == 0;
        }
      },
      error: (error: any) => console.error(error)
    });

  }
}
