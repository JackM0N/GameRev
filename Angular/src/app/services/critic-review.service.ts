import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { CriticReview } from '../interfaces/criticReview';
import { criticReviewFilters } from '../interfaces/criticReviewFilters';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
// Service for handling critics reviews
export class CriticReviewService {
  private baseUrl = 'http://localhost:8080/critics-reviews';
  private idUrl = 'http://localhost:8080/critics-reviews/id';
  private allUrl = 'http://localhost:8080/critics-reviews/list';
  private addUrl = 'http://localhost:8080/critics-reviews/create';
  private editUrl = 'http://localhost:8080/critics-reviews/edit';
  private reviewUrl = 'http://localhost:8080/critics-reviews/review'; // for approving/disapproving
  private deleteUrl = 'http://localhost:8080/critics-reviews/delete';

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) {}

  getCriticReviewsByGameTitle(gameTitle: string): Observable<CriticReview> {
    return this.http.get<CriticReview>(`${this.baseUrl}/${gameTitle}`);
  }

  getCriticReviewById(id: number): Observable<CriticReview> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.get<CriticReview>(`${this.idUrl}/${id}`, { headers });
  }

  getAllReviews(page: number, size: number, sortBy: string, sortDir: string, filters: criticReviewFilters): Observable<CriticReview[]> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    var params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sort', sortBy + ',' + sortDir
    );

    if (filters.startDate && filters.endDate) {
      params = params.set('fromDate', filters.startDate).set('toDate', filters.endDate);
    }
    if (filters.reviewStatus && filters.reviewStatus.length > 0) {
      params = params.set('reviewStatus', filters.reviewStatus.toString());
    }
    if (filters.search) {
      params = params.set('searchText', filters.search);
    }
    if (filters.scoreMin && filters.scoreMax) {
      params = params.set('scoreFrom', filters.scoreMin.toString()).set('scoreTo', filters.scoreMax.toString());
    }

    return this.http.get<CriticReview[]>(this.allUrl, { headers, params });
  }

  addCriticReview(criticReview: CriticReview): Observable<CriticReview> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.post<CriticReview>(this.addUrl, criticReview, { headers });
  }

  editCriticReview(criticReview: CriticReview): Observable<CriticReview> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.put<CriticReview>(`${this.editUrl}/${criticReview.id}`, criticReview, { headers });
  }

  reviewReview(criticReview: CriticReview): Observable<CriticReview> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    var params = new HttpParams();

    if (criticReview.reviewStatus) {
      params = new HttpParams()
      .set('reviewStatus', criticReview.reviewStatus);
    }

    return this.http.put<CriticReview>(`${this.reviewUrl}/${criticReview.id}`, criticReview.reviewStatus, { headers, params });
  }

  deleteReview(id: number): Observable<void> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.delete<void>(`${this.deleteUrl}/${id}`, { headers: headers });
  }
}
