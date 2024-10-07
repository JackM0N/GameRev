import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { ForumRequestService } from '../../../services/forumRequest.service';
import { NotificationService } from '../../../services/notification.service';
import { ForumRequest } from '../../../models/forumRequest';
import { MatPaginator } from '@angular/material/paginator';
import { AuthService } from '../../../services/auth.service';
import { MatTableDataSource } from '@angular/material/table';
import { BackgroundService } from '../../../services/background.service';
import { MatDialog } from '@angular/material/dialog';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { MatSelectChange } from '@angular/material/select';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ForumFormDialogComponent } from './forum-form-dialog.component';

@Component({
  selector: 'app-forum-requests',
  templateUrl: './forum-requests.component.html'
})
export class ForumRequestsComponent implements AfterViewInit {
  @ViewChild(MatPaginator) protected paginator!: MatPaginator;

  protected requestList: ForumRequest[] = [];
  protected dataSource: MatTableDataSource<ForumRequest> = new MatTableDataSource<ForumRequest>([]);
  protected totalRequests: number = 0;
  protected displayedColumns: string[] = ['forumName', 'description', 'game', 'parentForum', 'author', 'approved', 'options'];

  private approvedFilter?: boolean = undefined;
  protected filterForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private forumRequestService: ForumRequestService,
    private notificationService: NotificationService,
    private backgroundService: BackgroundService,
    protected authService: AuthService,
    protected dialog: MatDialog
  ) {
    this.filterForm = this.fb.group({
      approved: [undefined],
    });
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
    this.totalRequests = 0;

    const page = this.paginator ? this.paginator.pageIndex : 0;
    const size = this.paginator ? this.paginator.pageSize : 10;

    this.forumRequestService.getRequests(page, size, this.approvedFilter).subscribe({
      next: (response) => {
        if (response) {
          this.requestList = response.content;
          this.totalRequests = response.totalElements;
          this.dataSource = new MatTableDataSource<ForumRequest>(this.requestList);
        }

      },
      error: error => console.error(error)
    });
  }

  openAddNewRequestDialog() {
    const dialogRef = this.dialog.open(ForumFormDialogComponent, {
      width: '300px',
      data: {
        editing: false,
        requesting: true
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.loadForumRequests();
      }
    });
  }

  openEditRequestDialog(forumRequest: ForumRequest) {
    const dialogRef = this.dialog.open(ForumFormDialogComponent, {
      width: '300px',
      data: {
        editing: true,
        requesting: true,
        parentForumId: forumRequest.parentForum.id,
        id: forumRequest.id,
        name: forumRequest.forumName,
        description: forumRequest.description,
        gameTitle: forumRequest.game.title
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.loadForumRequests();
      }
    });
  }

  canEditRequest(forumRequest: ForumRequest) {
    return this.authService.hasAnyRole(['Admin']) || forumRequest.author.nickname;
  }

  approveRequest(forumRequest: ForumRequest, approve: boolean, action1: string, action2: string) {
    this.forumRequestService.approveRequest(forumRequest, approve).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Request ' + action2 + 'd successfully');
        this.loadForumRequests();
      },
      error: error => this.notificationService.popErrorToast('Request ' + action1 + ' failed', error)
    });
  }

  openApproveRequestDialog(request: ForumRequest, approve: boolean) {
    const action1 = approve ? 'approval' : 'disapproval';
    const action2 = approve ? 'approve' : 'disapprove';

    const dialogTitle = 'Forum request ' + action1;
    const dialogContent = 'Are you sure you want to ' + action2 + ' the request for forum ' + request.forumName + '?';
    const submitText = action2.charAt(0).toUpperCase() + action2.slice(1);
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '400px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        this.approveRequest(request, approve, action1, action2);
      }
    });
  }

  openDeleteRequestDialog(request: ForumRequest) {
    const dialogTitle = 'Forum request deletion';
    const dialogContent = 'Are you sure you want to delete the request for forum ' + request.forumName + '?';
    const submitText = 'Delete';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '400px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        this.deleteRequest(request);
      }
    });
  }

  deleteRequest(request: ForumRequest) {
    if (!request || !request.id) {
      console.error('Request ID is not valid.');
      return;
    }

    this.forumRequestService.deleteRequest(request).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Deleted request successfuly');
        this.loadForumRequests();
      },
      error: error => this.notificationService.popErrorToast('Deleting request failed', error)
    });
  }

  clearFilters() {
    this.filterForm.reset();
    this.approvedFilter = undefined;
    this.loadForumRequests();
  }

  onApprovedFilterChange(event: MatSelectChange) {
    this.approvedFilter = event.value;
    this.loadForumRequests();
  }
}
