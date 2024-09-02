import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Game } from '../interfaces/game';
import { gameFilters } from '../interfaces/gameFilters';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
// Service for handling games
export class GameService {
  private baseUrl = 'http://localhost:8080/games';
  private addUrl = 'http://localhost:8080/games/create';
  private editUrl = 'http://localhost:8080/games/edit';
  private deleteUrl = 'http://localhost:8080/games/delete';

  constructor(
    private authService: AuthService,
    private http: HttpClient,
  ) {}

  getGames(page?: number, size?: number, sortBy?: string, sortDir?: string, filters?: gameFilters): Observable<Game[]> {
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
      if (filters.startDate && filters.endDate) {
        params = params.set('fromDate', filters.startDate).set('toDate', filters.endDate);
      }
      if (filters.releaseStatus && filters.releaseStatus.length > 0) {
        params = params.set('releaseStatuses', filters.releaseStatus.toString());
      }
      if (filters.tags && filters.tags.length > 0) {
        params = params.set('tagIds', filters.tags.toString());
      }
      if (filters.search) {
        params = params.set('searchText', filters.search);
      }
      if (filters.scoreMin && filters.scoreMax) {
        params = params.set('minUserScore', filters.scoreMin.toString()).set('maxUserScore', filters.scoreMax.toString());
      }
    }

    return this.http.get<Game[]>(this.baseUrl, { params });
  }

  getGameByName(name: string): Observable<Game> {
    return this.http.get<Game>(`${this.baseUrl}/${name}`);
  }

  addGame(game: Game): Observable<Game> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.post<Game>(this.addUrl, game, { headers });
  }

  editGame(title: string, game: Game): Observable<Game> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.put<Game>(`${this.editUrl}/${title}`, game, { headers });
  }

  deleteGame(id: number): Observable<Game> {
    const token = this.authService.getToken();
    
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.delete<Game>(`${this.deleteUrl}/${id}`, { headers });
  }
}