import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Game } from '../interfaces/game';
import { UserGame } from '../interfaces/userGame';
import { libraryFilters } from '../interfaces/libraryFilters';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
// Service for handling user's game library
export class LibraryService {
  private baseUrl = 'http://localhost:8080/library';

  constructor(
    private authService: AuthService,
    private http: HttpClient,
  ) {}

  getUserGames(nickname: string, page: number, size: number, sortBy: string, sortDir: string, filters: libraryFilters): Observable<Game> {
    var params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sort', sortBy + ',' + sortDir
    );
    
    if (filters.isFavorite !== undefined) {
      params = params.set('isFavourite', filters.isFavorite.toString());
    }
    if (filters.tags && filters.tags.length > 0) {
      params = params.set('tagIds', filters.tags.toString());
    }
    if (filters.completionStatus && filters.completionStatus.length > 0) {
      params = params.set('completionStatus', filters.completionStatus.toString());
    }
    
    return this.http.get<Game>(`${this.baseUrl}/${nickname}`, {params});
  }

  updateUserGame(userReview: UserGame): Observable<UserGame> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<UserGame>(this.baseUrl, userReview, { headers: headers });
  }

  addUserGame(userReview: UserGame): Observable<UserGame> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.post<UserGame>(this.baseUrl, userReview, { headers: headers });
  }

  deleteUserGame(id: number): Observable<void> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers: headers });
  }
}
