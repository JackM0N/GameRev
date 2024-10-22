import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Report } from '../models/report';
import { UserReview } from '../models/userReview';
import { reviewFilters } from '../filters/reviewFilters';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
// Service for handling user reports of reviews
export class ReportService {
  private apiUrl: string = environment.apiUrl;

  private baseUrl = this.apiUrl + '/reports';
  private reportUrl = this.apiUrl + '/users-reviews/report';
  private approveUrl = this.apiUrl + '/reports/approve';
  private ownReports = this.apiUrl + '/reports/my-reports';

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) {}

  reportReview(report: Report): Observable<Report> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.put<Report>(this.reportUrl, report, { headers: headers });
  }

  getReviewsWithReports(page: number, size: number, sortBy: string, sortDir: string, filters: reviewFilters): Observable<UserReview[]> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();
    
    var params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sort', sortBy + ',' + sortDir
    );

    if (filters.startDate !== undefined && filters.endDate !== undefined) {
      params = params.set('postDateFrom', filters.startDate).set('postDateTo', filters.endDate);
    }
    if (filters.scoreMin && filters.scoreMax) {
      params = params.set('scoreFrom', filters.scoreMin.toString()).set('scoreTo', filters.scoreMax.toString());
    }

    return this.http.get<UserReview[]>(this.baseUrl, { headers, params });
  }

  getReportsForReview(reviewId: number, sortBy: string, sortDir: string, page?: number, size?: number): Observable<UserReview[]> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    var params = new HttpParams()
      .set('sort', sortBy + ',' + sortDir
    );

    if (page) {
      params = params.set('page', (page - 1).toString());
    }
    if (size) {
      params = params.set('size', size.toString());
    }

    return this.http.get<UserReview[]>(`${this.baseUrl}/${reviewId}`, { headers, params });
  }

  getOwnUserReports(sortBy?: string, sortDir?: string, page?: number, size?: number): Observable<UserReview[]> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    var params = new HttpParams();

    if (sortBy) {
      params = params.set('sort', sortBy + ',' + sortDir);
    }
    if (page) {
      params = params.set('page', (page - 1).toString());
    }
    if (size) {
      params = params.set('size', size.toString());
    }

    return this.http.get<UserReview[]>(this.ownReports, { headers, params });
  }

  approveReport(report: Report): Observable<Report> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    report.approved = true;
    return this.http.put<Report>(this.approveUrl, report, { headers: headers });
  }

  disapproveReport(report: Report): Observable<Report> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    report.approved = false;
    return this.http.put<Report>(this.approveUrl, report, { headers: headers });
  }

  deleteReport(report: Report): Observable<Report> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.delete<Report>(`${this.baseUrl}/${report.id}`, { headers: headers });
  }
}