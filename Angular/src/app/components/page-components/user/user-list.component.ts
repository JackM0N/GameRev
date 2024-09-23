import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, fromEvent, map, Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { MatSort } from '@angular/material/sort';
import { WebsiteUser } from '../../../interfaces/websiteUser';
import { UserService } from '../../../services/user.service';
import { AuthService } from '../../../services/auth.service';
import { NotificationService } from '../../../services/notification.service';
import { MatSelectChange } from '@angular/material/select';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { DatePipe } from '@angular/common';
import { Role } from '../../../interfaces/role';
import { userFilters } from '../../../interfaces/userFilters';
import { BackgroundService } from '../../../services/background.service';
import { FormBuilder, FormGroup } from '@angular/forms';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html'
})
export class UserListComponent extends BaseAdComponent implements OnInit, AfterViewInit {
  protected totalUsers: number = 0;
  protected dataSource: MatTableDataSource<WebsiteUser> = new MatTableDataSource<WebsiteUser>([]);
  protected displayedColumns: string[] = ['nickname', 'lastActionDate', 'description', 'joinDate', 'isBanned', 'roles', 'options'];
  protected isAdmin = false;

  @ViewChild(MatPaginator) protected paginator!: MatPaginator;
  @ViewChild(MatSort) protected sort!: MatSort;
  @ViewChild('searchInput', { static: true }) protected searchInput?: ElementRef;

  private filters: userFilters = {};
  protected filterForm: FormGroup;

  constructor(
    protected userService: UserService,
    private authService: AuthService,
    private backgroundService: BackgroundService,
    private fb: FormBuilder,
    private router: Router,
    private datePipe: DatePipe,
    protected dialog: MatDialog,
    private adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);

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

  override ngOnInit() {
    this.backgroundService.setClasses(['fallingCds']);

    this.isAdmin = this.authService.isAdmin();

    if (this.isAdmin) {
      this.adService.setAdVisible(false);
      this.displayedColumns = ['id', 'username', 'nickname', 'email', 'lastActionDate', 'description', 'joinDate', 'isBanned', 'isDeleted', 'roles', 'options'];
    } else {
      super.ngOnInit();
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
