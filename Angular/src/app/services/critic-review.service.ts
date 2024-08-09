import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { CriticReview } from '../interfaces/criticReview';

@Injectable({
  providedIn: 'root'
})
// Service for handling critics reviews
export class CriticReviewService {
  private baseUrl = 'http://localhost:8080/critics-reviews';
  private allUrl = 'http://localhost:8080/critics-reviews/list';
  private addUrl = 'http://localhost:8080/critics-reviews/create';
  private editUrl = 'http://localhost:8080/critics-reviews/edit';
  private reviewUrl = 'http://localhost:8080/critics-reviews/review'; // for approving/disapproving
  private deleteUrl = 'http://localhost:8080/critics-reviews/delete';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) {}

  getCriticReviewsByGameTitle(gameTitle: string): Observable<CriticReview[]> {
    return this.http.get<CriticReview[]>(`${this.baseUrl}/${gameTitle}`);
  }

  getAllReviews(token: string, page: number, size: number, sortBy: string, sortDir: string): Observable<CriticReview[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    const params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sort', sortBy + ',' + sortDir);
    return this.http.get<CriticReview[]>(this.allUrl, { headers, params });
  }

  addCriticReview(criticReview: CriticReview, token: string): Observable<CriticReview> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.post<CriticReview>(this.addUrl, criticReview, { headers });
  }

  editCriticReview(criticReview: CriticReview, token: string): Observable<CriticReview> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<CriticReview>(`${this.editUrl}/${criticReview.id}`, criticReview, { headers });
  }

  reviewReview(criticReview: CriticReview, token: string): Observable<CriticReview> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    var params = new HttpParams();

    if (criticReview.reviewStatus) {
      params = new HttpParams()
      .set('reviewStatus', criticReview.reviewStatus);
    }

    return this.http.put<CriticReview>(`${this.reviewUrl}/${criticReview.id}`, criticReview.reviewStatus, { headers, params });
  }

  deleteReview(id: number, token: string): Observable<void> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.delete<void>(`${this.deleteUrl}/${id}`, { headers: headers });
  }
}
