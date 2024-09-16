import { Component, Input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { merge } from 'rxjs';
import { AuthService } from '../../../../services/auth.service';
import { NotificationService } from '../../../../services/notification.service';
import { NewCredentials } from '../../../../interfaces/newCredentials';

@Component({
  selector: 'app-email-change',
  templateUrl: './email-change-component.html'
})
export class EmailChangeComponent {
  @Input() public passedEmail: string | null = null;
  
  protected changeEmailForm: FormGroup;
  protected emailErrorMessage = signal('');
  protected hidePassword = signal(true);

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private notificationService: NotificationService,
  ) {
    this.changeEmailForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      email: ['', [Validators.required, Validators.email]],
    });

    const email = this.changeEmailForm.get('email');

    if (email != null) {
      merge(email.statusChanges, email.valueChanges)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.updateEmailErrorMessage());
    }
  }

  ngOnChanges() {
    if (this.passedEmail) {
      this.changeEmailForm.get('email')?.setValue(this.passedEmail);
    }
  }

  onSubmitEmailChange() {
    if (this.changeEmailForm.valid) {
      const userName = this.authService.getUsername();

      if (!userName) {
        this.notificationService.popErrorToast('Email change failed', "Username not found");
        return;
      }

      const newData: NewCredentials = {
        username: userName,
        currentPassword: this.changeEmailForm.get('currentPassword')?.value,
        email: this.changeEmailForm.get('email')?.value,
      };

      this.authService.changeProfile(newData).subscribe({
        next: () => { this.notificationService.popSuccessToast('Email change successful'); },
        error: error => this.notificationService.popErrorToast('Email change failed', error)
      });
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

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }
}
