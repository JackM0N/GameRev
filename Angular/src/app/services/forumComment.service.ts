import { Injectable, SecurityContext } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ForumComment } from '../interfaces/forumComment';
import { forumCommentFilters } from '../interfaces/forumCommentFilters';
import { DomSanitizer } from '@angular/platform-browser';

@Injectable({
  providedIn: 'root'
})
// Service for handling forum comments
export class ForumCommentService {
  private baseUrl = 'http://localhost:8080/post';
  private addUrl = 'http://localhost:8080/post/create';
  private editUrl = 'http://localhost:8080/edit';
  private deleteUrl = 'http://localhost:8080/post/delete';

  constructor(
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

  editComment(token: string, comment: any): Observable<ForumComment> {
    const sanitizedComment = {
      ...comment,
      content: this.sanitizer.sanitize(SecurityContext.HTML, comment.content.trim())
    };

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<ForumComment>(`${this.editUrl}/${comment.id}`, sanitizedComment, { headers });
  }

  addComment(token: string, comment: any): Observable<ForumComment> {
    const sanitizedComment = {
      ...comment,
      content: this.sanitizer.sanitize(SecurityContext.HTML, comment.content.trim())
    };

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.post<ForumComment>(this.addUrl, sanitizedComment, { headers });
  }

  deleteComment(token: string, id: number): Observable<any> {
    const params = new HttpParams().set('isDeleted', true);

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.delete(`${this.deleteUrl}/${id}`, { headers, params });
  }
}