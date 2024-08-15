import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Tag } from '../interfaces/tag';

@Injectable({
  providedIn: 'root'
})
// Service for handling tags
export class TagService {
  private baseUrl = 'http://localhost:8080/tags';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) { }

  getTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.baseUrl);
  }
}