import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TimeEntryList } from './time-entry-list';

describe('TimeEntryList', () => {
  let component: TimeEntryList;
  let fixture: ComponentFixture<TimeEntryList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimeEntryList],
    }).compileComponents();

    fixture = TestBed.createComponent(TimeEntryList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
