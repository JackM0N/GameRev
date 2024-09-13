import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { merge } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { WebsiteUser } from '../../../interfaces/websiteUser';
import { AuthService } from '../../../services/auth.service';
import { passwordMatchValidator } from '../../../util/passwordMatchValidator';
import { BackgroundService } from '../../../services/background.service';
import { NotificationService } from '../../../services/notification.service';
import { NotificationAction } from '../../../enums/notificationActions';

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
  hideConfirmPassword = signal(true);

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private notificationService: NotificationService,
    private backgroundService: BackgroundService
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

  ngOnInit(): void {
    this.backgroundService.setClasses(['pinkStars']);
  }

  onSubmit() {
    if (this.registrationForm.valid) {
      const userData: WebsiteUser = {
        username: this.registrationForm.get('username')?.value,
        email: this.registrationForm.get('email')?.value,
        password: this.registrationForm.get('password')?.value,
        nickname: this.registrationForm.get('username')?.value,
      };

      this.authService.registerUser(userData).subscribe({
        next: () => {
          this.notificationService.popSuccessToast('Registration successful', NotificationAction.GO_TO_HOME);
        },
        error: error => this.notificationService.popErrorToast('Registration failed', error)
      });
    }
  }

  isEmailInvalid() {
    return this.registrationForm.get('email')?.invalid && this.registrationForm.get('email')?.touched;
  }

  updateEmailErrorMessage() {
    const email = this.registrationForm.get('email');

    if(email != null) {
      if (email.hasError('required')) {
        this.emailErrorMessage.set('Please provide an email address');
  
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

  hideConfirmPasswordClickEvent(event: MouseEvent) {
    this.hideConfirmPassword.set(!this.hideConfirmPassword());
    event.stopPropagation();
  }
}
