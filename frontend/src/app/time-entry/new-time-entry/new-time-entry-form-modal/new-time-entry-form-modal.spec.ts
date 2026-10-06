import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NewTimeEntryFormModal } from './new-time-entry-form-modal';

describe('NewTimeEntryFormModal', () => {
  let component: NewTimeEntryFormModal;
  let fixture: ComponentFixture<NewTimeEntryFormModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewTimeEntryFormModal],
    }).compileComponents();

    fixture = TestBed.createComponent(NewTimeEntryFormModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
