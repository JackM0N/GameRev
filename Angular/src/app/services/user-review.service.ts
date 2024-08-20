import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { UserReview } from '../interfaces/userReview';
import { reviewFilters } from '../interfaces/reviewFilters';

@Injectable({
  providedIn: 'root'
})
// Service for handling user reviews
export class UserReviewService {
  private baseUrl = 'http://localhost:8080/users-reviews';
  private getByIdUrl = 'http://localhost:8080/users-reviews/id';
  private ratingUrl = 'http://localhost:8080/users-reviews/add-rating';
  private ownReviews = 'http://localhost:8080/users-reviews/my-reviews';
  private adminReviews = 'http://localhost:8080/users-reviews/admin/';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) {}

  getUserReviews(): Observable<UserReview[]> {
    return this.http.get<UserReview[]>(this.baseUrl);
  }

  getOwnUserReviews(token: string, page: number, size: number, sortBy: string, sortDir: string): Observable<UserReview[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    const params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sort', sortBy + ',' + sortDir);
    return this.http.get<UserReview[]>(this.ownReviews, { headers, params });
  }

  getUserReviewsAdmin(userId: number, token: string, page: number, size: number, sortBy: string, sortDir: string): Observable<UserReview[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    const params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sort', sortBy + ',' + sortDir);
    return this.http.get<UserReview[]>(`${this.adminReviews}/${userId}`, { headers, params });
  }

  getUserReviewById(id: string): Observable<UserReview> {
    return this.http.get<UserReview>(`${this.getByIdUrl}/${id}`);
  }

  getUserReviewsForGame(name: string, token: string, page: number, size: number, sortBy: string, sortDir: string, filters: reviewFilters): Observable<UserReview[]> {
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

    return this.http.get<UserReview[]>(`${this.baseUrl}/${name}`, { headers, params });
  }

  addUserReview(userReview: UserReview, token: string): Observable<UserReview> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.post<UserReview>(this.baseUrl, userReview, { headers: headers });
  }

  editUserReview(userReview: UserReview, token: string): Observable<UserReview> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<UserReview>(this.baseUrl, userReview, { headers: headers });
  }

  deleteUserReview(userReview: UserReview, token: string): Observable<UserReview> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    const options = {
      headers: headers,
      body: userReview
    };
    return this.http.delete<UserReview>(this.baseUrl, options);
  }

  updateUserReviewRating(userReview: UserReview, token: string): Observable<UserReview> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<UserReview>(this.ratingUrl, userReview, { headers: headers });
  }
}