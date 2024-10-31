import { AbstractControl, ValidatorFn } from "@angular/forms";

export function trimmedValidator(minLength: number): ValidatorFn {
  return (control: AbstractControl): { [key: string]: boolean } | null => {
    const trimmedValue = control.value ? control.value.trim() : '';

    if (trimmedValue.length === 0) {
      return { 'whitespace': true };

    } else if (trimmedValue.length < minLength) {
      return { 'minLength': true };
    }
    
    return null;
  };
}
