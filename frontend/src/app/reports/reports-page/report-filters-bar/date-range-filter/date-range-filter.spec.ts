import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DateRangeFilter } from './date-range-filter';

describe('DateRangeFilter', () => {
  let component: DateRangeFilter;
  let fixture: ComponentFixture<DateRangeFilter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DateRangeFilter],
    }).compileComponents();

    fixture = TestBed.createComponent(DateRangeFilter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
