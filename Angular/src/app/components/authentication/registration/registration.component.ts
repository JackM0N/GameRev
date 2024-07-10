import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { merge, Observer } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { WebsiteUser } from '../../../interfaces/websiteuser';
import { AuthService } from '../../../services/auth.service';
import { passwordMatchValidator } from '../../../util/passwordMatchValidator';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrl: '/src/app/styles/shared-form-styles.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegistrationComponent {
  registrationForm: FormGroup;

  emailErrorMessage = signal('');
  passwordMismatchErrorMessage = signal('');
  hidePassword = signal(true);

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService
  ) {
    this.registrationForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(6)]]
    }, { validators: passwordMatchValidator });

    const email = this.registrationForm.get('email');
    const password = this.registrationForm.get('password');
    const confirmPassword = this.registrationForm.get('confirmPassword');

    if (email != null) {
      merge(email.statusChanges, email.valueChanges)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.updateEmailErrorMessage());
    }

    if (password && confirmPassword) {
      merge(password.valueChanges, confirmPassword.valueChanges)
        .pipe(takeUntilDestroyed())
        .subscribe(() => this.updatePasswordMismatchErrorMessage());
    }
  }

  onSubmit() {
    if (this.registrationForm.valid) {
      const userData: WebsiteUser = {
        username: this.registrationForm.get('username')?.value,
        email: this.registrationForm.get('email')?.value,
        password: this.registrationForm.get('password')?.value,
        nickname: this.registrationForm.get('username')?.value,
      };

      const observer: Observer<any> = {
        next: response => {
          console.log("Registration successful:", response);
        },
        error: error => {
          console.error("Registration failed:", error);
        },
        complete: () => {}
      };
      this.authService.registerUser(userData).subscribe(observer);
    }
  }

  isEmailInvalid() {
    return this.registrationForm.get('email')?.invalid && this.registrationForm.get('email')?.touched;
  }

  updateEmailErrorMessage() {
    const email = this.registrationForm.get('email');

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

  isPasswordMismatch() {
    return this.registrationForm.hasError('passwordMismatch');
  }

  updatePasswordMismatchErrorMessage() {
    if (this.isPasswordMismatch()) {
      this.passwordMismatchErrorMessage.set('Passwords do not match');
    } else {
      this.passwordMismatchErrorMessage.set('');
    }
  }

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }
}
