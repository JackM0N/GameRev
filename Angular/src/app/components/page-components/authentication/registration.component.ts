import { ChangeDetectionStrategy, ChangeDetectorRef, Component, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { merge } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { WebsiteUser } from '../../../models/websiteUser';
import { AuthService } from '../../../services/auth.service';
import { passwordMatchValidator } from '../../../validators/passwordMatchValidator';
import { BackgroundService } from '../../../services/background.service';
import { NotificationService } from '../../../services/notification.service';
import { NotificationAction } from '../../../enums/notificationActions';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegistrationComponent extends BaseAdComponent implements OnInit {
  registrationForm: FormGroup;

  emailErrorMessage = signal('');
  passwordMismatchErrorMessage = signal('');
  hidePassword = signal(true);
  hideConfirmPassword = signal(true);

  protected images = [
    // Left Side
    { classNames: 'smallBgImage1 animate-wiggle-slow absolute top-[12%] left-[15%]' },
    { classNames: 'smallBgImage2 animate-wiggle-reverse absolute top-[20%] left-[10%]' },
    { classNames: 'smallBgImage3 animate-wiggle absolute top-[35%] left-[25%]' },
    { classNames: 'smallBgImage4 animate-wiggle-slow absolute top-[50%] left-[12%]' },
    { classNames: 'smallBgImage5 animate-wiggle-reverse-slow absolute top-[60%] left-[8%]' },
    { classNames: 'smallBgImage6 animate-wiggle-reverse-slow absolute top-[75%] left-[18%]' },
    { classNames: 'smallBgImage7 animate-wiggle-slow absolute top-[90%] left-[20%]' },
    
    // Center
    { classNames: 'smallBgImage8 animate-wiggle-reverse absolute top-[10%] left-[45%]' },
    { classNames: 'smallBgImage9 animate-wiggle absolute top-[25%] left-[50%]' },
    { classNames: 'smallBgImage10 animate-wiggle-reverse-slow absolute top-[40%] left-[60%]' },
    { classNames: 'smallBgImage11 animate-wiggle-reverse absolute top-[55%] left-[55%]' },
    { classNames: 'smallBgImage12 animate-wiggle-slow absolute top-[70%] left-[40%]' },
    
    // Right Side
    { classNames: 'smallBgImage13 animate-wiggle-reverse-slow absolute top-[15%] left-[75%]' },
    { classNames: 'smallBgImage14 animate-wiggle-reverse absolute top-[30%] left-[80%]' },
    { classNames: 'smallBgImage15 animate-wiggle-reverse absolute top-[45%] left-[85%]' },
    { classNames: 'smallBgImage16 animate-wiggle-slow absolute top-[60%] left-[70%]' },
    { classNames: 'smallBgImage17 animate-wiggle absolute top-[75%] left-[85%]' },
    { classNames: 'smallBgImage18 animate-wiggle-reverse-slow absolute top-[90%] left-[65%]' },
  ];

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private notificationService: NotificationService,
    protected backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
    
    this.registrationForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email, Validators.minLength(4)]],
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

  override ngOnInit(): void {
    super.ngOnInit();
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
