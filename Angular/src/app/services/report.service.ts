import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Report } from '../interfaces/report';
import { UserReview } from '../interfaces/userReview';
import { reviewFilters } from '../interfaces/reviewFilters';

@Injectable({
  providedIn: 'root'
})
// Service for handling user reports of reviews
export class ReportService {
  private baseUrl = 'http://localhost:8080/reports';
  private reportUrl = 'http://localhost:8080/users-reviews/report';
  private approveUrl = 'http://localhost:8080/reports/approve';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) {}

  reportReview(report: Report, token: string): Observable<Report> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<Report>(this.reportUrl, report, { headers: headers });
  }

  getReviewsWithReports(token: string, page: number, size: number, sortBy: string, sortDir: string, filters: reviewFilters): Observable<UserReview[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
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

  getReportsForReview(reviewId: number, token: string, sortBy: string, sortDir: string, page?: number, size?: number): Observable<UserReview[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

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

  approveReport(report: Report, token: string): Observable<Report> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    report.approved = true;
    return this.http.put<Report>(this.approveUrl, report, { headers: headers });
  }

  disapproveReport(report: Report, token: string): Observable<Report> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    report.approved = false;
    return this.http.put<Report>(this.approveUrl, report, { headers: headers });
  }

  deleteReport(report: Report, token: string): Observable<Report> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.delete<Report>(`${this.baseUrl}/${report.id}`, { headers: headers });
  }

  deleteReview(review: UserReview, token: string): Observable<Report> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.delete<Report>(`${this.baseUrl}/${review.id}`, { headers: headers });
  }
}