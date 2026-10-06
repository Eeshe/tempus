import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NewTimeEntryButton } from './new-time-entry-button';

describe('NewTimeEntryButton', () => {
  let component: NewTimeEntryButton;
  let fixture: ComponentFixture<NewTimeEntryButton>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewTimeEntryButton],
    }).compileComponents();

    fixture = TestBed.createComponent(NewTimeEntryButton);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
