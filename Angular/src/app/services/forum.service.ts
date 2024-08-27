import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Forum } from '../interfaces/forum';
import { forumFilters } from '../interfaces/forumFilters';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
// Service for handling forums
export class ForumService {
  private baseUrl = 'http://localhost:8080/forum';
  private pathUrl = 'http://localhost:8080/path';
  private addUrl = 'http://localhost:8080/forum/create';
  private editUrl = 'http://localhost:8080/forum/edit';

  constructor(
    public authService: AuthService,
    private http: HttpClient,
  ) {}

  getForumPath(id: number): Observable<Forum> {
    return this.http.get<Forum>(`${this.pathUrl}/${id}`);
  }

  getForum(id?: number, page?: number, size?: number, sortBy?: string, sortDir?: string, filters?: forumFilters): Observable<Forum> {
    var params = new HttpParams();

    if (page) {
      params = params.set('page', (page - 1).toString());
    }
    if (size) {
      params = params.set('size', size.toString());
    }
    if (sortBy && sortDir) {
      params = params.set('sort', sortBy + ',' + sortDir);
    }
    if (filters) {
      if (filters.gameId) {
        params = params.set('gameId', filters.gameId);
      }
      if (filters.search) {
        params = params.set('searchText', filters.search);
      }
    }

    var url = this.baseUrl;
    if (id) {
      url += `/${id}`;
    }

    return this.http.get<Forum>(url, { params });
  }

  addForum(forum: Forum): Observable<Forum> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.post<Forum>(this.addUrl, forum, { headers });
  }

  editForum(forum: Forum): Observable<Forum> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.put<Forum>(`${this.editUrl}/${forum.id}`, forum, { headers });
  }
}