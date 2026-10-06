import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveTimeEntry } from './active-time-entry';

describe('ActiveTimeEntry', () => {
  let component: ActiveTimeEntry;
  let fixture: ComponentFixture<ActiveTimeEntry>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveTimeEntry],
    }).compileComponents();

    fixture = TestBed.createComponent(ActiveTimeEntry);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
