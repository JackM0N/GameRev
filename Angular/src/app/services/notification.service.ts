import { Injectable } from '@angular/core';
import { Toast, ToasterService } from 'angular-toaster';
import { Location } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  constructor(
    private toasterService: ToasterService,
    private _location: Location
  ) {}
  
  popSuccessToast(title: string, goBack: boolean = false) {
    if (goBack) {
      this._location.back();
    }
    var toast: Toast = {
      type: 'success',
      title: title,
      showCloseButton: true
    };
    this.toasterService.pop(toast);
  }

  popErrorToast(title: string, error?: string) {
    if (error) {
      console.error(error);
    }
    var toast: Toast = {
      type: 'error',
      title: title,
      showCloseButton: true
    };
    this.toasterService.pop(toast);
  }
}
