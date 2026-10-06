import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReportFiltersBar } from './report-filters-bar';

describe('ReportFiltersBar', () => {
  let component: ReportFiltersBar;
  let fixture: ComponentFixture<ReportFiltersBar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportFiltersBar],
    }).compileComponents();

    fixture = TestBed.createComponent(ReportFiltersBar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
