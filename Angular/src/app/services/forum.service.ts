import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Forum } from '../models/forum';
import { forumFilters } from '../filters/forumFilters';
import { AuthService } from './auth.service';
import { WebsiteUser } from '../models/websiteUser';
import { environment } from '../../environments/environment';
import { PaginatedResponse } from '../models/paginatedResponse';

@Injectable({
  providedIn: 'root'
})
// Service for handling forums
export class ForumService {
  private apiUrl: string = environment.apiUrl;
  
  private baseUrl = this.apiUrl + '/forum';
  private pathUrl = this.apiUrl + '/path';
  private addUrl = this.apiUrl + '/forum/create';
  private editUrl = this.apiUrl + '/forum/edit';
  private deleteUrl = this.apiUrl + '/forum/delete';
  private moderatorsUrl = this.apiUrl + '/forum/moderators';

  constructor(
    private authService: AuthService,
    private http: HttpClient,
  ) {}

  getForumPath(id: number): Observable<Forum[]> {
    return this.http.get<Forum[]>(`${this.pathUrl}/${id}`);
  }

  getForums(): Observable<Forum[]> {
    return this.http.get<Forum[]>(this.baseUrl);
  }

  getModerators(forumId: number): Observable<WebsiteUser[]> {
    return this.http.get<WebsiteUser[]>(`${this.moderatorsUrl}/${forumId}`);
  }

  getForum(id?: number, page?: number, size?: number, filters?: forumFilters): Observable<PaginatedResponse<Forum>> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    var params = new HttpParams();

    if (page) {
      params = params.set('page', (page - 1).toString());
    }
    if (size) {
      params = params.set('size', size.toString());
    }
    if (filters) {
      if (filters.gameId != undefined) {
        params = params.set('gameId', filters.gameId);
      }
      if (filters.search) {
        params = params.set('searchText', filters.search);
      }
      if (filters.isDeleted != undefined) {
        params = params.set('isDeleted', filters.isDeleted);
      }
    }

    var url = this.baseUrl;
    if (id) {
      url += `/${id}`;
    }

    return this.http.get<PaginatedResponse<Forum>>(url, { params, headers });
  }

  addForum(forum: Forum): Observable<Forum> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.post<Forum>(this.addUrl, forum, { headers });
  }

  editForum(forum: Forum): Observable<Forum> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();
    
    return this.http.put<Forum>(`${this.editUrl}/${forum.id}`, forum, { headers });
  }

  deleteForum(id: number, isDeleted: boolean = true): Observable<void> {
    const params = new HttpParams().set("isDeleted", isDeleted);
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.delete<void>(`${this.deleteUrl}/${id}`, { headers, params });
  }
}
