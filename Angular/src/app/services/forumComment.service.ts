import { Injectable, SecurityContext } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ForumComment } from '../models/forumComment';
import { forumCommentFilters } from '../filters/forumCommentFilters';
import { DomSanitizer } from '@angular/platform-browser';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';
import { StyleSanitizerUtil } from '../util/styleSanitizerUtil';
import { PaginatedResponse } from '../models/paginatedResponse';

@Injectable({
  providedIn: 'root'
})
// Service for handling forum comments
export class ForumCommentService {
  private apiUrl: string = environment.apiUrl;
  
  private baseUrl = this.apiUrl + '/post';
  private addUrl = this.apiUrl + '/post/create';
  private editUrl = this.apiUrl + '/post/edit';
  private deleteUrl = this.apiUrl + '/post/delete';

  constructor(
    public authService: AuthService,
    private http: HttpClient,
    private sanitizer: DomSanitizer
  ) {}

  getComments(id: number, page?: number, size?: number, sortBy?: string, sortDir?: string, filters?: forumCommentFilters): Observable<PaginatedResponse<ForumComment>> {
    let params = new HttpParams();

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

    return this.http.get<PaginatedResponse<ForumComment>>(`${this.baseUrl}/${id}`, { params });
  }

  editComment(comment: Partial<ForumComment>, picture?: File): Observable<ForumComment> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    const sanitizedComment = {
      ...comment,
      content: this.sanitizer.sanitize(SecurityContext.HTML, comment.content!.trim())
    };

    const formData = new FormData();
    formData.append('comment', JSON.stringify(sanitizedComment));
  
    if (picture) {
      formData.append('picture', picture);
    }

    return this.http.put<ForumComment>(`${this.editUrl}/${comment.id}`, formData, { headers });
  }

  addComment(comment: Partial<ForumComment>, picture?: File): Observable<ForumComment> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    const sanitizedComment = {
      ...comment,
      content: StyleSanitizerUtil.sanitizeContentAndReapplyStyles(comment.content!.trim(), this.sanitizer)
    };

    const formData = new FormData();
    formData.append('comment', JSON.stringify(sanitizedComment));
  
    if (picture) {
      formData.append('picture', picture);
    }

    return this.http.post<ForumComment>(this.addUrl, formData, { headers });
  }

  deleteComment(id: number): Observable<void> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    const params = new HttpParams().set('isDeleted', true);

    return this.http.delete<void>(`${this.deleteUrl}/${id}`, { headers, params });
  }
}
