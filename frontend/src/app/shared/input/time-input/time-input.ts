import { Component, ElementRef, input, model, output, viewChild } from '@angular/core';
import { FORM_FIELD, FormValueControl } from '@angular/forms/signals';

@Component({
  selector: 'app-time-input',
  providers: [{ provide: FORM_FIELD, useExisting: TimeInput }],
  styleUrl: './time-input.css',
  templateUrl: './time-input.html',
})
export class TimeInput implements FormValueControl<string | null> {
  private readonly inputElement = viewChild.required<ElementRef<HTMLInputElement>>('inputElement');

  readonly value = model<string | null>(null);
  readonly invalid = input<boolean>(false);
  readonly touch = output<void>();
  readonly timeChangeEvent = output<string | null>();

  protected onInput(event: Event): void {
    const input: HTMLInputElement = event.target as HTMLInputElement;
    const raw: string = input.value;
    const caret: number = input.selectionStart ?? raw.length;
    const digitAmountBeforeCaret: number = raw.slice(0, caret).replace(/\D/g, '').length;
    const digits: string = raw.replace(/\D/g, '').slice(0, 4);
    const formatted: string = digits === '' ? '' : formatDigits(digits);
    this.value.set(formatted === '' ? null : formatted);

    input.value = formatted;
    let pos: number = 0;
    let seen: number = 0;
    // Readjust caret
    while (pos < formatted.length && seen < digitAmountBeforeCaret) {
      if (/\d/.test(formatted[pos])) {
        seen++;
      }
      pos++;
    }
    input.setSelectionRange(pos, pos);
  }

  selectAllText(): void {
    this.inputElement().nativeElement.select();
  }

  protected onBlur(): void {
    this.touch.emit();
    this.timeChangeEvent.emit(this.value());
    this.inputElement().nativeElement.blur();
  }
}

function formatDigits(digits: string): string {
  if (digits.length <= 2) {
    return digits;
  }
  if (digits.length === 3) {
    return `${digits.slice(0, 2)}:${digits.slice(2)}`;
  }
  const pad = (n: number) => n.toString().padStart(2, '0');
  const hours: number = Math.min(23, Number(digits.slice(0, 2)));
  const minutes: number = Math.min(59, Number(digits.slice(2)));
  return `${pad(hours)}:${pad(minutes)}`;
}
