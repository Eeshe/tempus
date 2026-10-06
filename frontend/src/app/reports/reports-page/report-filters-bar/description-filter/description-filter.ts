import { Component, ElementRef, input, output, signal, viewChild } from '@angular/core';
import { PopupSelectorBase } from '../../../../shared/selector/popup-selector-base';

@Component({
  imports: [],
  selector: 'app-description-filter',
  styleUrl: './description-filter.css',
  templateUrl: './description-filter.html',
})
export class DescriptionFilter extends PopupSelectorBase {
  private readonly descriptionInput = viewChild<ElementRef<HTMLInputElement>>('descriptionInput');

  readonly descriptions = input<string[]>([]);

  readonly descriptionsChangeEvent = output<string[]>();

  readonly inputValue = signal<string>('');

  protected override openPopup(): void {
    super.openPopup();

    setTimeout(() => {
      this.descriptionInput()?.nativeElement.focus();
    });
  }

  onInput(event: Event): void {
    const target: HTMLInputElement = event.target as HTMLInputElement;

    this.inputValue.set(target.value);
  }

  addDescription(): void {
    const trimmed: string = this.inputValue().trim();
    if (trimmed === '') {
      return;
    }
    const current: string[] = this.descriptions();
    if (current.includes(trimmed)) {
      this.inputValue.set('');
      return;
    }
    this.descriptionsChangeEvent.emit(this.sortDescriptions([...current, trimmed]));

    this.inputValue.set('');
  }

  removeDescription(descriptionToRemove: string): void {
    const updated: string[] = this.descriptions().filter(
      (description: string) => description !== descriptionToRemove
    );
    this.descriptionsChangeEvent.emit(this.sortDescriptions(updated));
  }

  private sortDescriptions(descriptions: string[]): string[] {
    return [...descriptions].sort((a: string, b: string) => a.localeCompare(b, undefined, { sensitivity: 'base' }));
  }
}
