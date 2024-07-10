import { Component, OnInit, signal } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { merge, Observer } from 'rxjs';
import { NewCredentials } from '../../../interfaces/newCredentials';
import { passwordMatchValidator } from '../../../util/passwordMatchValidator';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

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
  
    if (!userName) {
      console.error("Username not found");
      return;
    }

    if (!token) {
      console.error("Token not found");
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        console.log("Profile information:", response);
        this.changeProfileInformationForm.get('nickname')?.setValue(response.nickname);
        this.changeProfileInformationForm.get('description')?.setValue(response.description);
        this.changeEmailForm.get('email')?.setValue(response.email);
      },
      error: error => {
        console.error("Profile information:", error);
      },
      complete: () => {}
    };
    this.authService.getUserProfileInformation(userName, token).subscribe(observer);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  deleteAccount() {
    //todo
    this.router.navigate(['/']);
  }

  onSubmitProfileInformationChange() {
    if (this.changeProfileInformationForm.valid) {
      const userName = this.authService.getUserName();
      const token = this.authService.getToken();

      if (!userName) {
        console.error("Username not found");
        return;
      }

      if (!token) {
        console.error("Token not found");
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
          console.log("Profile information change successful:", response);
        },
        error: error => {
          console.error("Profile information change failed:", error);
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

      if (!userName) {
        console.error("Username not found");
        return;
      }

      if (!token) {
        console.error("Token not found");
        return;
      }

      const newData: NewCredentials = {
        username: userName,
        currentPassword: this.changePasswordForm.get('currentPassword')?.value,
        newPassword: this.changePasswordForm.get('password')?.value,
      };

      const observer: Observer<any> = {
        next: response => {
          console.log("Password change successful:", response);
        },
        error: error => {
          console.error("Password change failed:", error);
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

      if (!userName) {
        console.error("Username not found");
        return;
      }

      if (!token) {
        console.error("Token not found");
        return;
      }

      const newData: NewCredentials = {
        username: userName,
        currentPassword: this.changeEmailForm.get('currentPassword')?.value,
        email: this.changeEmailForm.get('email')?.value,
      };

      const observer: Observer<any> = {
        next: response => {
          console.log("Profile information change successful:", response);
        },
        error: error => {
          console.error("Profile information change failed:", error);
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
