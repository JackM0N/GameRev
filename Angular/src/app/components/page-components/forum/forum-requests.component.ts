import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { ForumRequestService } from '../../../services/forumRequest.service';
import { NotificationService } from '../../../services/notification.service';
import { ForumRequest } from '../../../interfaces/forumRequest';
import { MatPaginator } from '@angular/material/paginator';
import { AuthService } from '../../../services/auth.service';
import { MatTableDataSource } from '@angular/material/table';
import { BackgroundService } from '../../../services/background.service';
import { ForumRequestFormDialogComponent } from './forum-request-form-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-forum-requests',
  templateUrl: './forum-requests.component.html'
})
export class ForumRequestsComponent implements AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  
  protected showOnlyApproved?: boolean = undefined;
  protected requestList: ForumRequest[] = [];
  protected dataSource: MatTableDataSource<ForumRequest> = new MatTableDataSource<ForumRequest>([]);
  protected displayedColumns: string[] = ['forumName', 'description', 'game', 'parentForum', 'author', 'approved', 'options'];

  constructor(
    private forumRequestService: ForumRequestService,
    private notificationService: NotificationService,
    private backgroundService: BackgroundService,
    protected authService: AuthService,
    public dialog: MatDialog
  ) {
  }

  ngOnInit(): void {
    this.backgroundService.setClasses(['fallingCds']);
    this.loadForumRequests();
  }

  ngAfterViewInit() {
    this.paginator.page.subscribe(() => {
      this.loadForumRequests();
    });
  }

  loadForumRequests() {
    this.requestList = [];

    const page = this.paginator ? this.paginator.pageIndex + 1 : 1;
    const size = this.paginator ? this.paginator.pageSize : 10;

    this.forumRequestService.getRequests(page, size, this.showOnlyApproved).subscribe({
      next: (response) => {
        if (response) {
          console.log(response);
          this.requestList = response.content;
          this.dataSource = new MatTableDataSource<ForumRequest>(this.requestList);
        }
      },
      error: error => console.error(error)
    });
  }

  openAddNewRequestDialog() {
    this.dialog.open(ForumRequestFormDialogComponent, {
      width: '300px',
      data: {
        editing: false
      }
    });
  }

  openEditRequestDialog(forumRequest: ForumRequest) {
    this.dialog.open(ForumRequestFormDialogComponent, {
      width: '300px',
      data: {
        editing: true,
        parentForum: forumRequest.parentForum,
        id: forumRequest.id,
        name: forumRequest.forumName,
        description: forumRequest.description,
        game: forumRequest.game
      }
    });
  }

  canEditRequest(forumRequest: ForumRequest) {
    return this.authService.hasAnyRole(['Admin']) || forumRequest.author.nickname;
  }

  openApproveRequestDialog(request: ForumRequest, approve: boolean) {

  }
}
