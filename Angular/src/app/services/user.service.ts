import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WebsiteUser } from '../interfaces/websiteUser';
import { userFilters } from '../interfaces/userFilters';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
// Service for handling users
export class UserService {
  private baseUrl = 'http://localhost:8080/user/list';
  private banUrl = 'http://localhost:8080/user/ban';
  private profileUrl = 'http://localhost:8080/user';

  constructor(
    private authService: AuthService,
    private http: HttpClient,
  ) {}

  getUsers(page: number, size: number, sortBy: string, sortDir: string, filters: userFilters): Observable<WebsiteUser> {
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
    
    return this.http.get<WebsiteUser>(this.baseUrl, { params });
  }

  getUser(nickname: string): Observable<WebsiteUser> {
    return this.http.get<WebsiteUser>(`${this.profileUrl}/${nickname}`);
  }

  banUser(user: WebsiteUser): Observable<WebsiteUser> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    user.isBanned = true;
    return this.http.put<WebsiteUser>(this.banUrl, user, { headers });
  }

  unbanUser(user: WebsiteUser): Observable<WebsiteUser> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    user.isBanned = false;
    return this.http.put<WebsiteUser>(this.banUrl, user, { headers });
  }

  getProfilePicture(nickname: string): Observable<Blob> {
    const token = this.authService.getToken();

    const url = `${this.profileUrl}/${nickname}/profile-picture`;

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.get<Blob>(url, {
      headers: headers,
      responseType: 'blob' as 'json'
    });
  }
}