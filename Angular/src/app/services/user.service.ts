import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
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

  getUsers(): Observable<WebsiteUser> {
    return this.http.get<WebsiteUser>(this.baseUrl);
  }

  getUser(): Observable<WebsiteUser> {
    return this.http.get<WebsiteUser>(this.profileUrl);
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
}