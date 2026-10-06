import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  afterNextRender,
  input,
  linkedSignal,
  output,
  viewChild,
} from '@angular/core';

@Component({
  imports: [],
  selector: 'app-inline-edit-input',
  styleUrl: './inline-edit-input.css',
  templateUrl: './inline-edit-input.html',
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class InlineEditInput {
  private readonly inputElement = viewChild.required<ElementRef<HTMLInputElement>>('inputElement');

  readonly value = input<string>('');
  readonly placeholder = input<string>('');

  readonly acceptEvent = output<string>();
  readonly cancelEvent = output<void>();

  readonly draft = linkedSignal(() => this.value());

  constructor() {
    afterNextRender(() => {
      const input: HTMLInputElement = this.inputElement().nativeElement;
      input.focus();
      input.setSelectionRange(input.value.length, input.value.length);
    });
  }

  updateDraft(event: Event): void {
    this.draft.set((event.target as HTMLInputElement).value);
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.acceptInput();
    } else if (event.key === 'Escape') {
      this.cancelInput();
    }
  }

  acceptInput(): void {
    const value: string = this.draft();
    if (value.trim() === '') {
      this.cancelInput();
      return;
    }
    this.acceptEvent.emit(value);
  }

  cancelInput(): void {
    this.cancelEvent.emit();
  }
}
