import { Component, ElementRef, input, model, output, viewChild } from '@angular/core';
import { FORM_FIELD, FormValueControl } from '@angular/forms/signals';

@Component({
  selector: 'app-date-input',
  providers: [{ provide: FORM_FIELD, useExisting: DateInput }],
  styleUrl: './date-input.css',
  templateUrl: './date-input.html',
})
export class DateInput implements FormValueControl<string | null> {
  readonly value = model<string | null>(null);
  readonly invalid = input<boolean>(false);
  readonly touch = output<void>();

  private readonly nativePicker = viewChild<ElementRef<HTMLInputElement>>('nativePicker');

  protected onInput(event: Event): void {
    const input: HTMLInputElement = event.target as HTMLInputElement;
    const digits: string = input.value.replace(/\D/g, '').slice(0, 8);

    this.value.set(digits === '' ? null : formatDigits(digits));
  }

  protected openPicker(): void {
    const picker = this.nativePicker();
    if (!picker) {
      return;
    }
    const element: HTMLInputElement = picker.nativeElement;
    element.value = this.value() ?? '';

    // const showPicker = (element as unknown as { showPicker?: () => void }).showPicker;
    const showPicker = element.showPicker;
    showPicker?.call(element);
  }

  protected onPickerChange(event: Event): void {
    const picked: string = (event.target as HTMLInputElement).value;

    this.value.set(picked === '' ? null : picked);
  }

  protected onBlur(): void {
    this.touch.emit();
  }
}

function formatDigits(digits: string): string {
  if (digits.length <= 4) {
    return digits;
  }
  if (digits.length <= 6) {
    return `${digits.slice(0, 4)}-${digits.slice(4)}`;
  }
  return `${digits.slice(0, 4)}-${digits.slice(4, 6)}-${digits.slice(6)}`;
}
