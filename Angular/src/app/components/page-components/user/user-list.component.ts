import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, fromEvent, map, Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { MatSort } from '@angular/material/sort';
import { WebsiteUser } from '../../../interfaces/websiteUser';
import { UserService } from '../../../services/user.service';
import { AuthService } from '../../../services/auth.service';
import { PopupDialogComponent } from '../../general-components/popup-dialog.component';
import { NotificationService } from '../../../services/notification.service';
import { MatSelectChange } from '@angular/material/select';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { DatePipe } from '@angular/common';
import { Role } from '../../../interfaces/role';
import { userFilters } from '../../../interfaces/userFilters';
import { BackgroundService } from '../../../services/background.service';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html'
})
export class UserListComponent implements OnInit, AfterViewInit {
  protected totalUsers: number = 0;
  protected dataSource: MatTableDataSource<WebsiteUser> = new MatTableDataSource<WebsiteUser>([]);
  protected displayedColumns: string[] = ['nickname', 'lastActionDate', 'description', 'joinDate', 'isBanned', 'isDeleted', 'roles', 'options'];
  protected isAdmin = false;

  @ViewChild(MatPaginator) protected paginator!: MatPaginator;
  @ViewChild(MatSort) protected sort!: MatSort;
  @ViewChild('searchInput', { static: true }) protected searchInput?: ElementRef;

  private filters: userFilters = {};
  protected filterForm: FormGroup;

  constructor(
    private userService: UserService,
    private notificationService: NotificationService,
    private authService: AuthService,
    private backgroundService: BackgroundService,
    private fb: FormBuilder,
    private router: Router,
    private datePipe: DatePipe,
    protected dialog: MatDialog
  ) {
    this.filterForm = this.fb.group({
      dateRange: this.fb.group({
        start: [null],
        end: [null]
      }),
      isBanned: [null],
      isDeleted: [null],
      roles: [null],
      search: [null]
    });
  }

  ngOnInit() {
    this.backgroundService.setClasses(['fallingCds']);

    this.isAdmin = this.authService.isAdmin();
    if (this.isAdmin) {
      this.displayedColumns = ['id', 'username', 'nickname', 'email', 'lastActionDate', 'description', 'joinDate', 'isBanned', 'isDeleted', 'roles', 'options'];
    }
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;

    this.loadUsers();

    this.paginator.page.subscribe(() => this.loadUsers());
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadUsers();
    });

    if (this.searchInput) {
      fromEvent(this.searchInput.nativeElement, 'input').pipe(
        map((event: any) => event.target.value),
        debounceTime(300),
        distinctUntilChanged()

      ).subscribe(value => {
        this.onSearchChange(value);
      });
    }
  }

  loadUsers() {
    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = this.sort.active || 'id';
    const sortDir = this.sort.direction || 'asc';

    const observer: Observer<any> = {
      next: response => {
        if (response) {
          this.totalUsers = response.totalElements;
          this.dataSource = new MatTableDataSource<WebsiteUser>(response.content);
        } else {
          this.totalUsers = 0;
          this.dataSource = new MatTableDataSource<WebsiteUser>([]);
        }
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    
    this.userService.getUsers(page, size, sortBy, sortDir, this.filters).subscribe(observer);
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  openBanDialog(user: WebsiteUser) {
    const dialogTitle = 'User banning';
    const dialogContent = 'Are you sure you want to ban user ' + user.username + '?';
    const submitText = 'Ban';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.banUser(user);
      }
    });
  }

  openUnbanDialog(user: WebsiteUser) {
    const dialogTitle = 'User unbanning';
    const dialogContent = 'Are you sure you want to unban user ' + user.username + '?';
    const submitText = 'Unban';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText  }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.unbanUser(user);
      }
    });
  }

  banUser(user: WebsiteUser) {
    this.userService.banUser(user).subscribe({
      next: () => { this.notificationService.popSuccessToast('User banned successfuly!'); },
      error: error => this.notificationService.popErrorToast('User ban failed', error)
    });
  }

  unbanUser(user: WebsiteUser) {
    this.userService.unbanUser(user).subscribe({
      next: () => { this.notificationService.popSuccessToast('User unbanned successfuly!'); },
      error: error => this.notificationService.popErrorToast('User unban failed', error)
    });
  }

  openProfile(user: WebsiteUser) {
    this.router.navigate(['/profile/' + user.nickname]);
  }

  openUserReviews(user: WebsiteUser) {
    this.router.navigate(['/user-reviews/' + user.id]);
  }

  onBannedFilterChange(event: MatSelectChange) {
    this.filters.isBanned = event.value;
    this.loadUsers();
  }

  onRolesFilterChange(event: MatSelectChange) {
    this.filters.roles = event.value;
    this.loadUsers();
  }

  onDeletedFilterChange(event: MatSelectChange) {
    this.filters.deleted = event.value;
    this.loadUsers();
  }

  onStartDateChange(event: MatDatepickerInputEvent<Date>) {
    const selectedDate = event.value;

    if (selectedDate) {
      const formattedDate = this.datePipe.transform(selectedDate, 'yyyy-MM-dd');
      
      if (formattedDate) {
        this.filters.startDate = formattedDate;
      }

      if (this.filters.startDate && this.filters.endDate) {
        this.loadUsers();
      }
    }
  }

  onEndDateChange(event: MatDatepickerInputEvent<Date>) {
    const selectedDate = event.value;

    if (selectedDate) {
      const formattedDate = this.datePipe.transform(selectedDate, 'yyyy-MM-dd');

      if (formattedDate) {
        this.filters.endDate = formattedDate;
      }

      if (this.filters.startDate && this.filters.endDate) {
        this.loadUsers();
      }
    }
  }

  onSearchChange(value: string) {
    this.filters.search = value;
    this.loadUsers();
  }

  parseRoles(roles: Role[]): string {
    return roles.map(role => role.roleName).join(', ');
  }

  clearFilters() {
    this.filters = {};
    this.filterForm.reset();
    this.loadUsers();
  }
}
