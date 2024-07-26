import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Report } from '../interfaces/report';
import { UserReview } from '../interfaces/userReview';

@Injectable({
  providedIn: 'root'
})
// Service for handling user reports of reviews
export class ReportService {
  private baseUrl = 'http://localhost:8080/reports';
  private reportUrl = 'http://localhost:8080/users-reviews/report';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) { }

  reportReview(report: Report, token: string): Observable<Report> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<Report>(this.reportUrl, report, { headers: headers });
  }
}