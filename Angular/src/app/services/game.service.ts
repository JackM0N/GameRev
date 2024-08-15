import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Game } from '../interfaces/game';

@Injectable({
  providedIn: 'root'
})
// Service for handling games
export class GameService {
  private baseUrl = 'http://localhost:8080/games';

  constructor(
    private http: HttpClient,
  ) { }

  getGames(page?: number, size?: number, sortBy?: string, sortDir?: string,
    startDateFilter?: string, endDateFilter?: string, releaseStatusFilter?: string[], tagsFilter?: string[], searchFilter?: string
  ): Observable<Game> {
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
    if (startDateFilter && endDateFilter) {
      params = params.set('fromDate', startDateFilter).set('toDate', endDateFilter);
    }
    if (releaseStatusFilter && releaseStatusFilter.length > 0) {
      params = params.set('releaseStatuses', releaseStatusFilter.toString());
    }
    if (tagsFilter && tagsFilter.length > 0) {
      params = params.set('tagIds', tagsFilter.toString());
    }
    if (searchFilter) {
      params = params.set('searchText', searchFilter);
    }

    return this.http.get<Game>(this.baseUrl, { params });
  }

  getGameByName(name: string): Observable<Game> {
    return this.http.get<Game>(`${this.baseUrl}/${name}`);
  }

  addGame(game: Game): Observable<Game> {
    return this.http.post<Game>(this.baseUrl, game);
  }

  editGame(title: string, game: Game): Observable<Game> {
    return this.http.put<Game>(`${this.baseUrl}/${title}`, game);
  }

  deleteGame(id: number): Observable<Game> {
    return this.http.delete<Game>(`${this.baseUrl}/${id}`);
  }
}