import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WebsiteUser } from '../interfaces/websiteuser';

@Injectable({
  providedIn: 'root'
})
// Service for handling CRUD operations on users
export class WebsiteUserService {
  private registerUrl = 'http://localhost:8080/register';

  constructor(
    private http: HttpClient,
  ) { }

  registerUser(userData: WebsiteUser): Observable<any> {
    return this.http.post<any>(this.registerUrl, userData);
  }
}