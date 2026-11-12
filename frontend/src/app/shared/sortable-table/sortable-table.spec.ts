import { Component, signal, TemplateRef, viewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SortableColumn, SortableTable } from './sortable-table';

interface Row {
  id: number;
  name: string;
}

@Component({
  imports: [SortableTable],
  template: `
    <app-sortable-table
      [columns]="columns"
      [rows]="rows"
      [trackBy]="trackBy"
      [cellTemplates]="{ name: nameCell }"
      [isRowExpanded]="isRowExpanded"
      [detailTemplate]="detail"
    >
      <ng-template #nameCell let-row>{{ row.name }}</ng-template>
      <ng-template #detail let-row><span class="detail-content">Detail {{ row.name }}</span></ng-template>
    </app-sortable-table>
  `,
})
class HostComponent {
  readonly rows: Row[] = [
    { id: 1, name: 'First' },
    { id: 2, name: 'Second' },
    { id: 3, name: 'Third' },
  ];
  readonly columns: SortableColumn<Row>[] = [
    { key: 'name', label: 'Name', align: 'left', sortValue: (row) => row.name },
  ];
  readonly expandedRowIds = signal<ReadonlySet<number>>(new Set<number>());

  readonly isRowExpanded = (row: Row): boolean => this.expandedRowIds().has(row.id);

  readonly trackBy = (_index: number, row: Row): number => row.id;

  readonly detail = viewChild.required<TemplateRef<unknown>>('detail');
  readonly nameCell = viewChild.required<TemplateRef<unknown>>('nameCell');
}

describe('SortableTable', () => {
  let component: SortableTable<unknown>;
  let fixture: ComponentFixture<SortableTable<unknown>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SortableTable],
    }).compileComponents();

    fixture = TestBed.createComponent(SortableTable);
    fixture.componentRef.setInput('columns', []);
    fixture.componentRef.setInput('rows', []);
    fixture.componentRef.setInput('trackBy', () => 0);
    fixture.componentRef.setInput('cellTemplates', {});
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('detail rows', () => {
    let hostFixture: ComponentFixture<HostComponent>;
    let hostElement: HTMLElement;

    beforeEach(async () => {
      hostFixture = TestBed.createComponent(HostComponent);
      hostElement = hostFixture.nativeElement;
      await hostFixture.whenStable();
    });

    it('does not render detail rows when no row is expanded', () => {
      expect(hostElement.querySelectorAll('.sortable-table__row--detail').length).toBe(0);
    });

    it('renders a full width detail row below an expanded row', async () => {
      hostFixture.componentInstance.expandedRowIds.set(new Set([2]));
      await hostFixture.whenStable();

      const detailRows: NodeListOf<HTMLTableRowElement> =
        hostElement.querySelectorAll('.sortable-table__row--detail');
      expect(detailRows.length).toBe(1);
      expect(detailRows[0].querySelector('td')?.getAttribute('colspan')).toBe('1');
      expect(detailRows[0].querySelector('.detail-content')?.textContent).toBe('Detail Second');

      const masterRows: NodeListOf<HTMLTableRowElement> =
        hostElement.querySelectorAll('.sortable-table__row:not(.sortable-table__row--detail)');
      expect(masterRows[1].nextElementSibling).toBe(detailRows[0]);
    });

    it('removes the detail row when the row is collapsed', async () => {
      hostFixture.componentInstance.expandedRowIds.set(new Set([2]));
      await hostFixture.whenStable();
      hostFixture.componentInstance.expandedRowIds.set(new Set());
      await hostFixture.whenStable();

      expect(hostElement.querySelectorAll('.sortable-table__row--detail').length).toBe(0);
    });

    it('stripes master rows and never stripes detail rows', async () => {
      hostFixture.componentInstance.expandedRowIds.set(new Set([2]));
      await hostFixture.whenStable();

      const masterRows: NodeListOf<HTMLTableRowElement> =
        hostElement.querySelectorAll('.sortable-table__row:not(.sortable-table__row--detail)');
      expect(masterRows[0].classList).not.toContain('sortable-table__row--striped');
      expect(masterRows[1].classList).toContain('sortable-table__row--striped');
      expect(masterRows[2].classList).not.toContain('sortable-table__row--striped');
      expect(
        hostElement
          .querySelector('.sortable-table__row--detail')
          ?.classList.contains('sortable-table__row--striped'),
      ).toBe(false);
    });
  });
});
