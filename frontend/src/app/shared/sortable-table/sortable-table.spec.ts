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
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
