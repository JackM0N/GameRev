import { Component, OnInit, signal } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { merge, Observer } from 'rxjs';
import { NewCredentials } from '../../../interfaces/newCredentials';
import { passwordMatchValidator } from '../../../util/passwordMatchValidator';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { LogoutConfirmationDialogComponent } from '../logout-confirmation-dialog/logout-confirmation-dialog.component';
import { Toast, ToasterService } from 'angular-toaster';
import { AccountDeletionConfirmationDialogComponent } from '../account-deletion-confirmation-dialog/account-deletion-confirmation-dialog.component';
import { WebsiteUser } from '../../../interfaces/websiteuser';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrl: '/src/app/styles/shared-form-styles.css'
})
export class ProfileComponent implements OnInit {
  changePasswordForm: FormGroup;
  changeEmailForm: FormGroup;
  changeProfileInformationForm: FormGroup;
  hidePassword = signal(true);
  passwordMismatchErrorMessage = signal('');
  emailErrorMessage = signal('');

  constructor(
    private authService: AuthService,
    private router: Router,
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private toasterService: ToasterService,
  ) {
    this.changePasswordForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
    }, { validators: passwordMatchValidator });

    this.changeEmailForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      email: ['', [Validators.required, Validators.email]],
    });
    
    this.changeProfileInformationForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      nickname: ['', [Validators.minLength(3)]],
      description: [''],
    });

    const email = this.changeEmailForm.get('email');

    if (email != null) {
      merge(email.statusChanges, email.valueChanges)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.updateEmailErrorMessage());
    }
  }

  ngOnInit(): void {
    const userName = this.authService.getUserName();
    const token = this.authService.getToken();
  
    if (!userName || !token) {
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        this.changeProfileInformationForm.get('nickname')?.setValue(response.nickname);
        this.changeProfileInformationForm.get('description')?.setValue(response.description);
        this.changeEmailForm.get('email')?.setValue(response.email);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'Unable to retrieve profile information',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {}
    };
    this.authService.getUserProfileInformation(userName, token).subscribe(observer);
  }

  openLogoutDialog() {
    const dialogRef = this.dialog.open(LogoutConfirmationDialogComponent);

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.logout();
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
    var toast: Toast = {
      type: 'success',
      title: 'Successfully logged out',
      showCloseButton: true
    };
    this.toasterService.pop(toast);
  }

  openAccountDeletionDialog() {
    const dialogRef = this.dialog.open(AccountDeletionConfirmationDialogComponent);

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteAccount(dialogRef);
      }
    });
  }

  deleteAccount(dialogRef: MatDialogRef<AccountDeletionConfirmationDialogComponent>) {
    const userName = this.authService.getUserName();
    const token = this.authService.getToken();
  
    if (!userName || !token || !dialogRef || !dialogRef.componentRef) {
      return;
    }

    const password = dialogRef.componentRef.instance.deleteAccountForm.get('currentPassword')?.value;

    const userData: NewCredentials = {
      username: userName,
      isDeleted: true,
      currentPassword: password,
    };

    console.log(userData);

    const observer: Observer<any> = {
      next: response => {
        this.authService.logout();
        var toast: Toast = {
          type: 'success',
          title: 'Account deleted successfuly!',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      error: error => {
        console.error(error);
        var toast: Toast = {
          type: 'error',
          title: 'Account deletion failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
      },
      complete: () => {}
    };
    this.authService.deleteOwnAccount(userData, token).subscribe(observer);

    this.router.navigate(['/']);
  }

  onSubmitProfileInformationChange() {
    if (this.changeProfileInformationForm.valid) {
      const userName = this.authService.getUserName();
      const token = this.authService.getToken();

      if (!userName || !token) {
        console.error("Username or token not found");
        var toast: Toast = {
          type: 'error',
          title: 'Profile information change failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
        return;
      }

      const newData: NewCredentials = {
        username: userName,
        currentPassword: this.changeProfileInformationForm.get('currentPassword')?.value,
        nickname: this.changeProfileInformationForm.get('nickname')?.value,
        description: this.changeProfileInformationForm.get('description')?.value,
      };

      const observer: Observer<any> = {
        next: response => {
          var toast: Toast = {
            type: 'success',
            title: 'Profile information change successful!',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        error: error => {
          console.error(error);
          var toast: Toast = {
            type: 'error',
            title: 'Profile information change failed',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        complete: () => {}
      };
      this.authService.changeProfile(newData, token).subscribe(observer);
    }
  }

  onSubmitPasswordChange() {
    if (this.changePasswordForm.valid) {
      const userName = this.authService.getUserName();
      const token = this.authService.getToken();

      if (!userName || !token) {
        console.error("Username or token not found");
        var toast: Toast = {
          type: 'error',
          title: 'Profile information change failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
        return;
      }

      const newData: NewCredentials = {
        username: userName,
        currentPassword: this.changePasswordForm.get('currentPassword')?.value,
        newPassword: this.changePasswordForm.get('password')?.value,
      };

      const observer: Observer<any> = {
        next: response => {
          var toast: Toast = {
            type: 'success',
            title: 'Password change successful',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        error: error => {
          console.error(error);
          var toast: Toast = {
            type: 'error',
            title: 'Password change failed',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        complete: () => {}
      };
      this.authService.changeProfile(newData, token).subscribe(observer);
    }
  }

  onSubmitEmailChange() {
    if (this.changeEmailForm.valid) {
      const userName = this.authService.getUserName();
      const token = this.authService.getToken();

      if (!userName || !token) {
        console.error("Username or token not found");
        var toast: Toast = {
          type: 'error',
          title: 'Profile information change failed',
          showCloseButton: true
        };
        this.toasterService.pop(toast);
        return;
      }

      const newData: NewCredentials = {
        username: userName,
        currentPassword: this.changeEmailForm.get('currentPassword')?.value,
        email: this.changeEmailForm.get('email')?.value,
      };

      const observer: Observer<any> = {
        next: response => {
          var toast: Toast = {
            type: 'success',
            title: 'Email change successful',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        error: error => {
          console.error(error);
          var toast: Toast = {
            type: 'error',
            title: 'Email change failed',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        complete: () => {}
      };
      this.authService.changeProfile(newData, token).subscribe(observer);
    }
  }

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }

  isPasswordMismatch() {
    return this.changePasswordForm.hasError('passwordMismatch');
  }

  updatePasswordMismatchErrorMessage() {
    if (this.isPasswordMismatch()) {
      this.passwordMismatchErrorMessage.set('Passwords do not match');
    } else {
      this.passwordMismatchErrorMessage.set('');
    }
  }

  updateEmailErrorMessage() {
    const email = this.changeEmailForm.get('email');

    if(email != null) {
      if (email.hasError('required')) {
        this.emailErrorMessage.set('You must enter a value');
  
      } else if (email.hasError('email')) {
        this.emailErrorMessage.set('Not a valid email');
  
      } else {
        this.emailErrorMessage.set('');
      }
    }
  }

  isEmailInvalid() {
    return this.changeEmailForm.get('email')?.invalid && this.changeEmailForm.get('email')?.touched;
  }
}
