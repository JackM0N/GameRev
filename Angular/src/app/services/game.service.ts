import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Game } from '../models/game';
import { gameFilters } from '../filters/gameFilters';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
// Service for handling games
export class GameService {
  private apiUrl: string = environment.apiUrl;

  private baseUrl = this.apiUrl + '/games';
  private addUrl = this.apiUrl + '/games/create';
  private editUrl = this.apiUrl + '/games/edit';
  private deleteUrl = this.apiUrl + '/games/delete';

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

  addGame(game: Game, picture?: string): Observable<Game> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    const formData = new FormData();
    formData.append('game', JSON.stringify(game));

    if (picture) {
      formData.append('picture', picture);
    }

    return this.http.post<Game>(this.addUrl, formData, { headers });
  }

  editGame(title: string, game: Game, picture?: string): Observable<Game> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    const formData = new FormData();
    formData.append('game', JSON.stringify(game));

    if (picture) {
      formData.append('picture', picture);
    }

    return this.http.put<Game>(`${this.editUrl}/${title}`, formData, { headers });
  }

  deleteGame(id: number): Observable<Game> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.delete<Game>(`${this.deleteUrl}/${id}`, { headers });
  }
}