import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SortableTable } from './sortable-table';

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
});
