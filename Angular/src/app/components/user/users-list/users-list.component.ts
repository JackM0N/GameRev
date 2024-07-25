import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { MatSort, Sort } from '@angular/material/sort';
import { WebsiteUser } from '../../../interfaces/websiteUser';
import { UserService } from '../../../services/user.service';
import { PopupDialogComponent } from '../../popup-dialog/popup-dialog.component';
import { Toast, ToasterService } from 'angular-toaster';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  styleUrl: '/src/app/styles/shared-list-styles.css'
})
export class UsersListComponent implements AfterViewInit {
  usersList: WebsiteUser[] = [];
  totalUsers: number = 0;
  dataSource: MatTableDataSource<WebsiteUser> = new MatTableDataSource<WebsiteUser>(this.usersList);
  displayedColumns: string[] = ['id', 'username', 'nickname', 'email', 'lastActionDate', 'description', 'joinDate', 'isBanned', 'isDeleted', 'options'];
  
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private userService: UserService,
    public dialog: MatDialog,
    private toasterService: ToasterService,
    private authService: AuthService,
    private router: Router,
  ) {
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;

    this.loadUsers();

    this.paginator.page.subscribe(() => this.loadUsers());
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadUsers();
    });
  }

  loadUsers() {
    const page = this.paginator.pageIndex + 1;
    const size = this.paginator.pageSize;
    const sortBy = this.sort.active || 'id';
    const sortDir = this.sort.direction || 'asc';

    const observer: Observer<any> = {
      next: response => {
        this.usersList = response.content;
        this.totalUsers = response.totalElements;
        this.dataSource = new MatTableDataSource<WebsiteUser>(this.usersList);
        this.dataSource.data = this.usersList;
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.userService.getUsers(page, size, sortBy, sortDir).subscribe(observer);
  }

  sortData(sort: Sort) {
    this.loadUsers();
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

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.unbanUser(user);
      }
    });
  }

  banUser(user: WebsiteUser) {
    const token = this.authService.getToken();

    if (token === null) {
      console.error('Token is null');
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        var toast: Toast = {
          type: 'success',
          title: 'User banned successfuly!',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'User ban failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {}
    };
    this.userService.banUser(user, token).subscribe(observer);
  }

  unbanUser(user: WebsiteUser) {
    const token = this.authService.getToken();

    if (token === null) {
      console.error('Token is null');
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        var toast: Toast = {
          type: 'success',
          title: 'User unbanned successfuly!',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'User unban failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {}
    };
    this.userService.unbanUser(user, token).subscribe(observer);
  }

  openProfile(user: WebsiteUser) {
    this.router.navigate(['/profile/' + user.nickname]);
  }
}
