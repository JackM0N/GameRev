import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { ForumRequest } from '../interfaces/forumRequest';

@Injectable({
  providedIn: 'root'
})
// Service for handling forum requests
export class ForumRequestService {
  private baseUrl = 'http://localhost:8080/forum-request';
  private listUrl = 'http://localhost:8080/forum-request/list';
  private addUrl = 'http://localhost:8080/forum-request/create';
  private editUrl = 'http://localhost:8080/forum-request/edit';
  private deleteUrl = 'http://localhost:8080/forum-request/delete';
  private approveUrl = 'http://localhost:8080/forum-request/approve';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  getRequests(page?: number, size?: number, approved?: boolean): Observable<any> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    var params = new HttpParams();

    if (page) {
      params = params.set('page', page.toString());
    }
    if (size) {
      params = params.set('size', size.toString());
    }
    if (approved != undefined) {
      params = params.set('approved', approved.toString());
    }

    return this.http.get<any>(this.listUrl, { params, headers });
  }

  addRequest(request: ForumRequest): Observable<any> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.post<any>(this.addUrl, request, { headers });
  }

  editRequest(request: ForumRequest): Observable<any> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.put<any>(this.editUrl, request, { headers });
  }

  approveRequest(request: ForumRequest, approved: boolean): Observable<any> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    var params = new HttpParams().set('approved', approved.toString());

    return this.http.put<any>(`${this.approveUrl}/${request.id}`, request, { headers, params });
  }

  deleteRequest(request: ForumRequest): Observable<any> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.delete<any>(`${this.deleteUrl}/${request.id}`, { headers });
  }
}
