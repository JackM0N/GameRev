import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginCredentials } from '../models/loginCredentials';
import { isPlatformBrowser } from '@angular/common';
import { JwtHelperService } from '@auth0/angular-jwt';
import { NewCredentials } from '../models/newCredentials';
import { Role } from '../models/role';
import { WebsiteUser } from '../models/websiteUser';
import { environment } from '../../environments/environment';
import { AuthResponse } from '../models/authResponse';

@Injectable({
  providedIn: 'root'
})
// Service for handling website authentication
export class AuthService {
  private tokenKey = 'gamerev_access_token';
  private apiUrl: string = environment.apiUrl;
  
  private registerUrl = this.apiUrl + '/register';
  private loginUrl = this.apiUrl + '/login';
  private profileChangeUrl = this.apiUrl + '/user/edit-profile';
  private profileInformationUrl = this.apiUrl + '/user/account';
  private profileChangePictureUrl = this.apiUrl + '/user';
  private requestPasswordResetUrl = this.apiUrl + '/password-reset/request';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService,
    @Inject(PLATFORM_ID) private platformId: number
  ) {
    if (!isPlatformBrowser(this.platformId)) {
      console.log('Not running in the browser');
      console.log('Platform ID:', this.platformId);
    }
  }

  registerUser(userData: WebsiteUser): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.registerUrl, userData).pipe(
      tap(response => {
        const token = response.token;
        if (token && isPlatformBrowser(this.platformId)) {
          localStorage.setItem(this.tokenKey, token);
        }
      })
    );
  }

  login(credentials: LoginCredentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.loginUrl, credentials).pipe(
      tap(response => {
        const token = response.token;
        if (token && isPlatformBrowser(this.platformId)) {
          localStorage.setItem(this.tokenKey, token);
        }
      })
    );
  }
  
  isAuthenticated(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem(this.tokenKey);
      return token !== null && !this.jwtHelper.isTokenExpired(token);
    }
    return false;
  }

  getToken(): string | null {
    const token = localStorage.getItem(this.tokenKey);

    if (this.jwtHelper.isTokenExpired(token)) {
      localStorage.removeItem(this.tokenKey);
      return null;
    }

    return isPlatformBrowser(this.platformId) ? token : null;
  }
  

  logout() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.tokenKey);
    }
  }

  getUsername(): string | undefined {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem(this.tokenKey);
      if (token) {
        const decodedToken = this.jwtHelper.decodeToken(token);
        return decodedToken.sub
      }
    }
    return undefined;
  }

  getNickname(): string | undefined {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem(this.tokenKey);
      if (token) {
        const decodedToken = this.jwtHelper.decodeToken(token);
        return decodedToken.nickname
      }
    }
    return undefined;
  }

  getRoles(): Role[] {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem(this.tokenKey);
      if (token) {
        const decodedToken = this.jwtHelper.decodeToken(token);
        return decodedToken.roles || [];
      }
    }
    return [];
  }
  
  hasRole(role: string): boolean {
    const roles = this.getRoles();
    return roles.some(rl => rl.roleName === role);
  }
  
  hasAnyRole(roles: string[]): boolean {
    const userRoles = this.getRoles();
    return userRoles.some(rl1 => roles.some(rl2 => rl1.roleName === rl2));
  }
  
  isAdmin(): boolean {
    return this.hasRole('Admin');
  }

  getUserProfileInformation(): Observable<WebsiteUser> {
    const token = this.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    return this.http.get<WebsiteUser>(this.profileInformationUrl, { headers });
  }

  changeProfile(userData: NewCredentials): Observable<any> {
    const token = this.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.put<WebsiteUser>(`${this.profileChangeUrl}/${userData.username}`, userData, { headers });
  }

  changeProfilePicture(username: string, profilePicture: File): Observable<any> {
    const token = this.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();

    const formData = new FormData();
    formData.append('file', profilePicture, profilePicture.name);

    return this.http.post<any>(`${this.profileChangePictureUrl}/${username}/profile-picture`, formData, {
      headers: headers
    });
  }

  deleteOwnAccount(userData: NewCredentials): Observable<any> {
    const token = this.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.put<WebsiteUser>(`${this.profileChangeUrl}/${userData.username}`, userData, { headers });
  }

  requestPasswordReset(email: string): Observable<any> {
    const url = `${this.requestPasswordResetUrl}?email=${encodeURIComponent(email)}`;
    return this.http.post<any>(url, {});
  }
}
