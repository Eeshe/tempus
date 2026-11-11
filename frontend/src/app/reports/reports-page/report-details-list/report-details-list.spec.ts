import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectReport, Report } from '../../models/report.model';
import { ReportDetailsList } from './report-details-list';

describe('ReportDetailsList', () => {
  let component: ReportDetailsList;
  let fixture: ComponentFixture<ReportDetailsList>;

  const mockReport: Report<ProjectReport> = {
    startDate: null,
    endDate: null,
    totalTrackedTimeMillis: 0,
    totalBillableTrackedTimeMillis: 0,
    totalNonBillableTrackedTimeMillis: 0,
    totalAccumulatedPay: 0,
    reportDays: [],
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportDetailsList],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReportDetailsList);
    fixture.componentRef.setInput('report', mockReport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders the accumulated pay with the currency symbol', () => {
    const header: HTMLElement = fixture.nativeElement.querySelector('.total-header');

    expect(header.textContent).toContain('$0.00');
    expect(header.textContent).not.toContain('USD');
  });
});
