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
  ) { }

  getUsers(page: number, size: number, sortBy: string, sortDir: string): Observable<WebsiteUser> {
    const params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<WebsiteUser>(this.baseUrl, {params});
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