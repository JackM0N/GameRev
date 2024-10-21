import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const allowedRoles = route.data['roles'] as Array<string>;
    
  if (authService.isAuthenticated()) {
    if (allowedRoles && !authService.hasAnyRole(allowedRoles)) {
      return false;
    }
    return true;
  }

  router.navigate(['/login']);
  return false;
};
