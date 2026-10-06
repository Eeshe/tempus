import { NgClass, NgTemplateOutlet } from '@angular/common';
import { Component, computed, input, output, signal, TemplateRef } from '@angular/core';

export interface SortableColumn<T> {
  key: string;
  label: string;
  align: 'left' | 'center' | 'right';
  cellAlign?: 'left' | 'center' | 'right';
  sortValue: (entry: T) => string | number;
}

@Component({
  imports: [NgClass, NgTemplateOutlet],
  selector: 'app-sortable-table',
  styleUrl: './sortable-table.css',
  templateUrl: './sortable-table.html',
})
export class SortableTable<T> {
  readonly columns = input.required<SortableColumn<T>[]>();
  readonly rows = input.required<T[]>();
  readonly trackBy = input.required<(index: number, entry: T) => unknown>();
  readonly cellTemplates = input.required<Record<string, TemplateRef<unknown>>>();
  readonly clickable = input<boolean>(false);
  readonly sortGroup = input<(entry: T) => number>(() => 0);
  readonly rowClass = input<(entry: T) => string | null>(() => null);

  readonly rowClickEvent = output<T>();

  readonly sortColumn = signal<string>('');
  readonly sortDirection = signal<'asc' | 'desc'>('asc');

  readonly activeSortColumn = computed<string>(() => {
    const selected = this.sortColumn();
    return selected !== '' ? selected : (this.columns()[0]?.key ?? '');
  });

  readonly sortedRows = computed<T[]>(() => {
    const rows = this.rows();
    const column = this.columns().find((column) => column.key === this.activeSortColumn());
    const sortGroup = this.sortGroup();
    if (column == null) {
      return [...rows].sort((entryA, entryB) => sortGroup(entryA) - sortGroup(entryB));
    }
    const sign: number = this.sortDirection() === 'asc' ? 1 : -1;
    return [...rows].sort((entryA, entryB) => {
      const groupComparison = sortGroup(entryA) - sortGroup(entryB);
      if (groupComparison !== 0) {
        return groupComparison;
      }

      const valueA = column.sortValue(entryA);
      const valueB = column.sortValue(entryB);

      return compareSortValues(valueA, valueB) * sign;
    });
  });

  toggleSort(newSortColumn: string): void {
    if (newSortColumn !== this.activeSortColumn()) {
      this.sortColumn.set(newSortColumn);
      this.sortDirection.set('asc');
      return;
    }
    this.sortDirection.update((current) => (current === 'asc' ? 'desc' : 'asc'));
  }
}

function compareSortValues(valueA: string | number, valueB: string | number): number {
  if (typeof valueA === 'number' && typeof valueB === 'number') {
    return (valueA ?? 0) - (valueB ?? 0);
  }
  if (typeof valueA === 'string' && typeof valueB === 'string') {
    return valueA.localeCompare(valueB, undefined, { sensitivity: 'base' });
  }
  return 0;
}
