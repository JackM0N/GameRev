import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WebsiteUser } from '../models/websiteUser';
import { userFilters } from '../filters/userFilters';
import { AuthService } from './auth.service';
import { PopupDialogComponent } from '../components/general-components/popup-dialog.component';
import { NotificationService } from './notification.service';
import { MatDialog } from '@angular/material/dialog';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
// Service for handling users
export class UserService {
  private apiUrl: string = environment.apiUrl;

  private baseUrl = this.apiUrl + '/user/list';
  private banUrl = this.apiUrl + '/user/ban';
  private profileUrl = this.apiUrl + '/user';

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private notificationService: NotificationService,
    protected dialog: MatDialog,
  ) {}

  getUsers(page: number, size: number, sortBy: string, sortDir: string, filters: userFilters): Observable<WebsiteUser> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    let params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sort', sortBy + ',' + sortDir
    );
  
    if (filters.isBanned !== undefined) {
      params = params.set('isBanned', filters.isBanned.toString());
    }
    if (filters.deleted !== undefined) {
      params = params.set('isDeleted', filters.deleted.toString());
    }
    if (filters.roles !== undefined && filters.roles.length > 0) {
      params = params.set('roleIds', filters.roles.toString());
    }
    if (filters.startDate !== undefined && filters.endDate !== undefined) {
      params = params.set('joinDateFrom', filters.startDate).set('joinDateTo', filters.endDate);
    }
    if (filters.search !== undefined) {
      params = params.set('searchText', filters.search);
    }
    
    return this.http.get<WebsiteUser>(this.baseUrl, { params, headers });
  }

  getUser(nickname: string): Observable<WebsiteUser> {
    return this.http.get<WebsiteUser>(`${this.profileUrl}/${nickname}`);
  }

  banUser(user: WebsiteUser): Observable<WebsiteUser> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    user.isBanned = true;
    return this.http.put<WebsiteUser>(this.banUrl, user, { headers });
  }

  unbanUser(user: WebsiteUser): Observable<WebsiteUser> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    user.isBanned = false;
    return this.http.put<WebsiteUser>(this.banUrl, user, { headers });
  }

  getProfilePicture(nickname: string): Observable<Blob> {
    const url = `${this.profileUrl}/${nickname}/profile-picture`;

    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.get<Blob>(url, {
      headers: headers,
      responseType: 'blob' as 'json'
    });
  }

  openBanDialog(user: WebsiteUser) {
    const dialogTitle = 'User banning';
    const dialogContent = 'Are you sure you want to ban user ' + user.nickname + '?';
    const submitText = 'Ban';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.banUser(user).subscribe({
          next: () => { this.notificationService.popSuccessToast('User banned successfuly!'); },
          error: error => this.notificationService.popErrorToast('User ban failed', error)
        });
      }
    });
  }

  openUnbanDialog(user: WebsiteUser) {
    const dialogTitle = 'User unbanning';
    const dialogContent = 'Are you sure you want to unban user ' + user.nickname + '?';
    const submitText = 'Unban';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText  }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.unbanUser(user).subscribe({
          next: () => { this.notificationService.popSuccessToast('User unbanned successfuly!'); },
          error: error => this.notificationService.popErrorToast('User unban failed', error)
        });
      }
    });
  }
}