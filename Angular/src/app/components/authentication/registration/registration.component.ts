import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { merge, Observer } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { WebsiteUser } from '../../../interfaces/websiteuser';
import { WebsiteUserService } from '../../../services/websiteuser.service';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrl: './registration.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegistrationComponent {
  registrationForm: FormGroup;

  emailErrorMessage = signal('');
  passwordMismatchErrorMessage = signal('');
  hidePassword = signal(true);

  fileError: string | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private websiteUserService: WebsiteUserService
  ) {
    this.registrationForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
      //nickname: ['', [Validators.maxLength(20)]],
      //description: ['', [Validators.maxLength(100)]],
      //profilepic: [null]
    }, { validators: this.passwordMatchValidator });

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
      this.websiteUserService.registerUser(userData).subscribe(observer);
      
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
    //return this.registrationForm.hasError('passwordMismatch') && this.registrationForm.get('confirmPassword')?.touched;
    return this.registrationForm.hasError('passwordMismatch');
  }

  updatePasswordMismatchErrorMessage() {
    console.log("updatePasswordMismatchErrorMessage: " + this.isPasswordMismatch());

    if (this.isPasswordMismatch()) {
      this.passwordMismatchErrorMessage.set('Passwords do not match');
    } else {
      this.passwordMismatchErrorMessage.set('');
    }
  }

  passwordMatchValidator: ValidatorFn = (group: AbstractControl):  ValidationErrors | null => { 
    let pass = group.get('password')?.value;
    let confirmPass = group.get('confirmPassword')?.value

    return pass === confirmPass ? null : { passwordMismatch: true }
  }

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }

  /*
  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      const file = input.files[0];
      if (file.size > 5000000) { // 5 MB limit
        this.fileError = 'File size should be less than 5 MB';
      } else {
        this.fileError = null;
        this.registrationForm.patchValue({ photo: file });
      }
    }
  }
  */
}
