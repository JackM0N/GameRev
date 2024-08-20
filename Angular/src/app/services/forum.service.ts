import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Forum } from '../interfaces/forum';
import { forumFilters } from '../interfaces/forumFilters';

@Injectable({
  providedIn: 'root'
})
// Service for handling forums
export class ForumService {
  private baseUrl = 'http://localhost:8080/forum';

  constructor(
    private http: HttpClient,
  ) {}

  getForum(id: number, page?: number, size?: number, sortBy?: string, sortDir?: string, filters?: forumFilters): Observable<Forum> {
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

    return this.http.get<Forum>(`${this.baseUrl}/${id}`, { params });
  }
}