import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observer } from 'rxjs';
import { AuthService } from '../../../services/auth.service';
import { LoginCredentials } from '../../../interfaces/loginCredentials';
import { Router } from '@angular/router';
import { Toast, ToasterService } from 'angular-toaster';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: '/src/app/styles/shared-form-styles.css'
})
export class LoginComponent {
  loginForm: FormGroup;
  hidePassword = signal(true);

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private toasterService: ToasterService,
    private router: Router
  ) {
    this.loginForm = this.formBuilder.group({
      usernameOrEmail: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  isValidEmail(email: string): boolean {
    const emailControl = this.formBuilder.control(email, Validators.email);
    return emailControl.valid;
  }

  onSubmit() {
    if (this.loginForm.valid) {
      const usernameOrEmail = this.loginForm.get('usernameOrEmail')?.value;

      const credentials: LoginCredentials = {
        username: undefined,
        email: undefined,
        password: this.loginForm.get('password')?.value,
      };

      if (this.isValidEmail(usernameOrEmail)) {
        credentials.email = usernameOrEmail;
      } else {
        credentials.username = usernameOrEmail;
      }

      const observer: Observer<any> = {
        next: response => {
          this.router.navigate(['/']);
          var toast: Toast = {
            type: 'success',
            title: 'Login successful',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        error: error => {
          console.error(error);
          var toast: Toast = {
            type: 'error',
            title: 'Login failed',
            showCloseButton: true
          };
          this.toasterService.pop(toast);
        },
        complete: () => {}
      };
      this.authService.login(credentials).subscribe(observer);
    }
  }

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }
}
