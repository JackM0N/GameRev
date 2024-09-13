import { Injectable, SecurityContext } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ForumComment } from '../interfaces/forumComment';
import { forumCommentFilters } from '../interfaces/forumCommentFilters';
import { DomSanitizer } from '@angular/platform-browser';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
// Service for handling forum comments
export class ForumCommentService {
  private baseUrl = 'http://localhost:8080/post';
  private addUrl = 'http://localhost:8080/post/create';
  private editUrl = 'http://localhost:8080/post/edit';
  private deleteUrl = 'http://localhost:8080/post/delete';

  constructor(
    public authService: AuthService,
    private http: HttpClient,
    private sanitizer: DomSanitizer
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

  editComment(comment: any): Observable<ForumComment> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    const sanitizedComment = {
      ...comment,
      content: this.sanitizer.sanitize(SecurityContext.HTML, comment.content.trim())
    };

    return this.http.put<ForumComment>(`${this.editUrl}/${comment.id}`, sanitizedComment, { headers });
  }

  addComment(comment: any, picture?: File): Observable<ForumComment> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    const sanitizedComment = {
      ...comment,
      content: this.sanitizer.sanitize(SecurityContext.HTML, comment.content.trim())
    };

    const formData = new FormData();
    formData.append('comment', JSON.stringify(sanitizedComment));
  
    if (picture) {
      formData.append('picture', picture);
    }

    return this.http.post<ForumComment>(this.addUrl, formData, { headers });
  }

  deleteComment(id: number): Observable<any> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    const params = new HttpParams().set('isDeleted', true);

    return this.http.delete(`${this.deleteUrl}/${id}`, { headers, params });
  }
}