import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TimeEntryBillableButton } from './time-entry-billable-button';

describe('TimeEntryBillableButton', () => {
  let component: TimeEntryBillableButton;
  let fixture: ComponentFixture<TimeEntryBillableButton>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimeEntryBillableButton],
    }).compileComponents();

    fixture = TestBed.createComponent(TimeEntryBillableButton);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
