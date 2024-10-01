import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { forumPostFilters } from '../filters/forumPostFilters';
import { ForumPost } from '../models/forumPost';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
// Service for handling forum posts
export class ForumPostService {
  private apiUrl: string = environment.apiUrl;
  
  private baseUrl = this.apiUrl + '/forum-post';
  private postByIdUrl = this.apiUrl + '/post/origin';
  private deleteUrl = this.apiUrl + '/forum-post/delete';
  private addUrl = this.apiUrl + '/forum-post/create';
  private editUrl = this.apiUrl + '/forum-post/edit';
  private pictureUrl = this.apiUrl + '/forum-post/picture';

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
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();
  
    const formData = new FormData();
    formData.append('post', JSON.stringify(post));
  
    if (picture) {
      formData.append('picture', picture);
    }
  
    return this.http.post(this.addUrl, formData, { headers });
  }
  
  editPost(post: ForumPost, picture?: File): Observable<any> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();
  
    const formData = new FormData();
    formData.append('post', JSON.stringify(post));
  
    if (picture) {
      formData.append('picture', picture);
    }

    return this.http.put(`${this.editUrl}/${post.id}`, formData, { headers });
  }

  deletePost(id: number): Observable<any> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    var params = new HttpParams().set('isDeleted', true);

    return this.http.delete(`${this.deleteUrl}/${id}`, { headers, params });
  }

  getPicture(id: number): Observable<Blob> {
    return this.http.get<Blob>(`${this.pictureUrl}/${id}`, {
      responseType: 'blob' as 'json'
    });
  }
}