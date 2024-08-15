import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WebsiteUser } from '../interfaces/websiteUser';

@Injectable({
  providedIn: 'root'
})
// Service for handling users
export class UserService {
  private baseUrl = 'http://localhost:8080/user/list';
  private banUrl = 'http://localhost:8080/user/ban';
  private profileUrl = 'http://localhost:8080/user';

  constructor(
    private http: HttpClient,
  ) {}

  getUsers(page: number, size: number, sortBy: string, sortDir: string,
      isBannedFilter?: boolean, deletedFilter?: boolean, rolesFilter?: string[],
      startDateFilter?: string, endDateFilter?: string, searchFilter?: string): Observable<WebsiteUser>
    {
    let params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sort', sortBy + ',' + sortDir);
  
      if (isBannedFilter !== undefined) {
        params = params.set('isBanned', isBannedFilter.toString());
      }
      if (deletedFilter !== undefined) {
        params = params.set('isDeleted', deletedFilter.toString());
      }
      if (rolesFilter !== undefined && rolesFilter.length > 0) {
        params = params.set('roleIds', rolesFilter.toString());
      }
      if (startDateFilter !== undefined && endDateFilter !== undefined) {
        params = params.set('joinDateFrom', startDateFilter).set('joinDateTo', endDateFilter);
      }
      if (searchFilter !== undefined) {
        params = params.set('searchText', searchFilter);
      }
  
    return this.http.get<WebsiteUser>(this.baseUrl, { params });
  }

  getUser(nickname: string): Observable<WebsiteUser> {
    return this.http.get<WebsiteUser>(`${this.profileUrl}/${nickname}`);
  }

  banUser(user: WebsiteUser, token: string): Observable<WebsiteUser> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    user.isBanned = true;
    return this.http.put<WebsiteUser>(this.banUrl, user, { headers });
  }

  unbanUser(user: WebsiteUser, token: string): Observable<WebsiteUser> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    user.isBanned = false;
    return this.http.put<WebsiteUser>(this.banUrl, user, { headers });
  }

  getProfilePicture(nickname: string, token: string): Observable<Blob> {
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