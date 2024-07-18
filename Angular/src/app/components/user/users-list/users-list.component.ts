import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { Observer } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { Sort } from '@angular/material/sort';
import { WebsiteUser } from '../../../interfaces/websiteUser';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  styleUrl: '/src/app/styles/shared-list-styles.css'
})
export class UsersListComponent implements AfterViewInit, OnInit {
  usersList: WebsiteUser[] = [];
  sortedData: WebsiteUser[] = [];
  dataSource: MatTableDataSource<WebsiteUser> = new MatTableDataSource<WebsiteUser>(this.usersList);
  displayedColumns: string[] = ['id', 'username', 'nickname', 'email', 'lastActionDate', 'description', 'joinDate', 'isBanned', 'isDeleted'];
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private userService: UserService,
    private router: Router,
    public dialog: MatDialog,
  ) {
  }

  ngOnInit() {
    const observer: Observer<any> = {
      next: response => {
        this.usersList = response;
        this.dataSource = new MatTableDataSource<WebsiteUser>(this.usersList);
      },
      error: error => {
        console.error(error);
      },
      complete: () => {}
    };
    this.userService.getUsers().subscribe(observer);
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  sortData(sort: Sort) {
    const data = this.usersList.slice();
    if (!sort.active || sort.direction === '') {
      this.sortedData = data;
      return;
    }

    this.sortedData = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case 'id':
          if(a.id && b.id) {
            return this.compare(a.id, b.id, isAsc);
          }
          return 1;
        case 'username':
          if (a.username && b.username) {
            return this.compare(a.username, b.username, isAsc);
          }
          return 1;
        case 'nickname':
          if (a.username && b.username) {
            return this.compare(a.username, b.username, isAsc);
          }
          return 1;
        case 'email':
          if (a.username && b.username) {
            return this.compare(a.username, b.username, isAsc);
          }
          return 1;
        case 'lastActionDate':
          if (a.lastActionDate && b.lastActionDate) {
            return this.compare(a.lastActionDate, b.lastActionDate, isAsc);
          }
          return 1;
        default:
          return 0;
      }
    });

    this.dataSource = new MatTableDataSource<WebsiteUser>(this.sortedData);
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }
}
