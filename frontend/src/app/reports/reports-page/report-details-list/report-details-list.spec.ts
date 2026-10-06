import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReportDetailsList } from './report-details-list';

describe('ReportDetailsList', () => {
  let component: ReportDetailsList;
  let fixture: ComponentFixture<ReportDetailsList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportDetailsList],
    }).compileComponents();

    fixture = TestBed.createComponent(ReportDetailsList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
