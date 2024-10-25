import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { UserReview } from '../models/userReview';
import { reviewFilters } from '../filters/reviewFilters';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';
import { PaginatedResponse } from '../models/paginatedResponse';

@Injectable({
  providedIn: 'root'
})
// Service for handling user reviews
export class UserReviewService {
  private apiUrl: string = environment.apiUrl;

  private baseUrl = this.apiUrl + '/users-reviews';
  private getByIdUrl = this.apiUrl + '/users-reviews/id';
  private ratingUrl = this.apiUrl + '/users-reviews/add-rating';
  private ownReviews = this.apiUrl + '/users-reviews/my-reviews';
  private adminReviews = this.apiUrl + '/users-reviews/admin/';

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) {}

  getUserReviews(): Observable<UserReview[]> {
    return this.http.get<UserReview[]>(this.baseUrl);
  }

  getOwnUserReviews(page: number, size: number, sortBy: string, sortDir: string): Observable<UserReview[]> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    const params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sort', sortBy + ',' + sortDir
    );
    return this.http.get<UserReview[]>(this.ownReviews, { headers, params });
  }

  getUserReviewsAdmin(userId: number, page: number, size: number, sortBy: string, sortDir: string): Observable<UserReview[]> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    const params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sort', sortBy + ',' + sortDir
    );
    return this.http.get<UserReview[]>(`${this.adminReviews}/${userId}`, { headers, params });
  }

  getUserReviewById(id: string): Observable<UserReview> {
    return this.http.get<UserReview>(`${this.getByIdUrl}/${id}`);
  }

  getUserReviewsForGame(name: string, page: number, size: number, sortBy: string, sortDir: string, filters: reviewFilters): Observable<PaginatedResponse<UserReview>> {
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

    return this.http.get<PaginatedResponse<UserReview>>(`${this.baseUrl}/${name}`, { params, headers });
  }

  addUserReview(userReview: UserReview): Observable<UserReview> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.post<UserReview>(this.baseUrl, userReview, { headers: headers });
  }

  editUserReview(userReview: UserReview): Observable<UserReview> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.put<UserReview>(this.baseUrl, userReview, { headers: headers });
  }

  deleteUserReview(review: UserReview): Observable<void> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();
    
    return this.http.delete<void>(`${this.baseUrl}/${review.id}`, { headers: headers });
  }

  updateUserReviewRating(userReview: UserReview): Observable<UserReview> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.put<UserReview>(this.ratingUrl, userReview, { headers: headers });
  }
}
