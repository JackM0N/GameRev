import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { forumPostFilters } from '../interfaces/forumPostFilters';
import { ForumPost } from '../interfaces/forumPost';

@Injectable({
  providedIn: 'root'
})
// Service for handling forum posts
export class ForumPostService {
  private baseUrl = 'http://localhost:8080/forum-post';
  private postByIdUrl = 'http://localhost:8080/forum-post/origin';

  constructor(
    private http: HttpClient,
  ) {}

  getPost(id: number): Observable<ForumPost[]> {
    return this.http.get<ForumPost[]>(`${this.postByIdUrl}/${id}`);
  }

  getPosts(id: number, page?: number, size?: number, sortBy?: string, sortDir?: string, filters?: forumPostFilters): Observable<ForumPost[]> {
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
      if (filters.startDate !== undefined && filters.endDate !== undefined) {
        params = params.set('postDateFrom', filters.startDate).set('postDateTo', filters.endDate);
      }
      if (filters.search) {
        params = params.set('searchText', filters.search);
      }
    }

    return this.http.get<ForumPost[]>(`${this.baseUrl}/${id}`, { params });
  }
}