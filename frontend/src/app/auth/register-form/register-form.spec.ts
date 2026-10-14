import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { RegisterForm } from './register-form';

describe('RegisterForm', () => {
  let component: RegisterForm;
  let fixture: ComponentFixture<RegisterForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterForm],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
