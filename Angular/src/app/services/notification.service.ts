import { Injectable } from '@angular/core';
import { Toast, ToasterService } from 'angular-toaster';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { NotificationAction } from '../enums/notificationActions';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  constructor(
    private toasterService: ToasterService,
    private _location: Location,
    private router: Router,
  ) {}
  
  popSuccessToast(title: string, afterAction?: NotificationAction) {
    if (afterAction == NotificationAction.GO_BACK) {
      this._location.back();

    } else if (afterAction == NotificationAction.REFRESH) {
      location.reload();
      
    } else if (afterAction == NotificationAction.GO_TO_HOME) {
      this.router.navigate(['/']);
    }
    
    const toast: Toast = {
      type: 'success',
      title: title,
      showCloseButton: true
    };
    this.toasterService.pop(toast);
  }

  popErrorToast(title: string, error?: HttpErrorResponse | string) {
    if (error) {
      console.error(error);
    }
    const toast: Toast = {
      type: 'error',
      title: title,
      showCloseButton: true
    };
    this.toasterService.pop(toast);
  }
}
