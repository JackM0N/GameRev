import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Tag } from '../models/tag';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
// Service for handling tags
export class TagService {
  private apiUrl: string = environment.apiUrl;

  private baseUrl = this.apiUrl + '/tags';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) {}

  getTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.baseUrl);
  }
}