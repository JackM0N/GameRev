import { AbstractControl, ValidatorFn } from '@angular/forms';

export function quillTextLengthValidator(minLength: number): ValidatorFn {
  return (control: AbstractControl) => {
    if (!control.value) {
      return { required: true };
    }

    // Strip HTML tags using regex
    const plainText = control.value.replace(/<\/?[^>]+(>|$)/g, '').trim();

    if (plainText.length < minLength) {
      return { minLength: true };
    }

    return null;
  };
}
