import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { forumPostFilters } from '../interfaces/forumPostFilters';
import { ForumPost } from '../interfaces/forumPost';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
// Service for handling forum posts
export class ForumPostService {
  private baseUrl = 'http://localhost:8080/forum-post';
  private postByIdUrl = 'http://localhost:8080/post/origin';
  private deleteUrl = 'http://localhost:8080/forum-post/delete';
  private addUrl = 'http://localhost:8080/forum-post/create';
  private editUrl = 'http://localhost:8080/forum-post/edit';
  private pictureUrl = 'http://localhost:8080/forum-post/picture';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
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

  addPost(post: ForumPost, picture?: File): Observable<any> {
    const token = this.authService.getToken();
  
    const formData = new FormData();
    formData.append('post', JSON.stringify(post));
  
    if (picture) {
      formData.append('picture', picture);
    }
  
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  
    return this.http.post(this.addUrl, formData, { headers });
  }
  
  editPost(post: ForumPost, picture?: File): Observable<any> {
    const token = this.authService.getToken();
  
    const formData = new FormData();
    formData.append('post', JSON.stringify(post));
  
    if (picture) {
      formData.append('picture', picture);
    }
  
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.put(`${this.editUrl}/${post.id}`, formData, { headers });
  }

  deletePost(id: number): Observable<any> {
    const token = this.authService.getToken();

    var params = new HttpParams().set('isDeleted', true);

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.delete(`${this.deleteUrl}/${id}`, { headers, params });
  }

  getPicture(id: number): Observable<Blob> {
    return this.http.get<Blob>(`${this.pictureUrl}/${id}`, {
      responseType: 'blob' as 'json'
    });
  }
}