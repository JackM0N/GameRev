import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WebsiteUser } from '../interfaces/websiteUser';

@Injectable({
  providedIn: 'root'
})
// Service for handling users
export class UserService {
  private baseUrl = 'http://localhost:8080/user/list';

  constructor(
    private http: HttpClient,
  ) { }

  getUsers(): Observable<WebsiteUser> {
    return this.http.get<WebsiteUser>(this.baseUrl);
  }
}