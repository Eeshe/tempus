import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimeEntryDescription } from './time-entry-description';

describe('TimeEntryDescription', () => {
  let component: TimeEntryDescription;
  let fixture: ComponentFixture<TimeEntryDescription>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimeEntryDescription],
    }).compileComponents();

    fixture = TestBed.createComponent(TimeEntryDescription);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
