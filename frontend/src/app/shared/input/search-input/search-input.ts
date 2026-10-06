import { Component, input, linkedSignal, output } from '@angular/core';

@Component({
  imports: [],
  selector: 'app-search-input',
  styleUrl: './search-input.css',
  templateUrl: './search-input.html',
})
export class SearchInput {
  readonly value = input<string>('');
  readonly placeholder = input<string>('Search');

  readonly searchChangeEvent = output<string>();

  readonly draft = linkedSignal(() => this.value());

  updateDraft(event: Event): void {
    const text: string = (event.target as HTMLInputElement).value;

    this.draft.set(text);
    this.searchChangeEvent.emit(text);
  }
}
