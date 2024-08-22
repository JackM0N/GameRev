import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const allowedRoles = route.data['roles'] as Array<string>;
    
    if (this.authService.isAuthenticated()) {
      if (allowedRoles && !this.authService.hasAnyRole(allowedRoles)) {
        return false;
      }
      return true;
    }

    this.router.navigate(['/login']);
    return false;
  }
}
