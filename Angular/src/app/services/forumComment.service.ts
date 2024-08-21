import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ForumComment } from '../interfaces/forumComment';
import { forumCommentFilters } from '../interfaces/forumCommentFilters';

@Injectable({
  providedIn: 'root'
})
// Service for handling forum comments
export class ForumCommentService {
  private baseUrl = 'http://localhost:8080/post';

  constructor(
    private http: HttpClient,
  ) {}

  getComments(id: number, page?: number, size?: number, sortBy?: string, sortDir?: string, filters?: forumCommentFilters): Observable<ForumComment[]> {
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
      if (filters.userId !== undefined) {
        params = params.set('userId', filters.userId);
      }
      if (filters.search) {
        params = params.set('searchText', filters.search);
      }
    }

    return this.http.get<ForumComment[]>(`${this.baseUrl}/${id}`, { params });
  }
}