import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreateProjectFormModal } from './create-project-form-modal';

describe('CreateProjectFormModal', () => {
  let component: CreateProjectFormModal;
  let fixture: ComponentFixture<CreateProjectFormModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateProjectFormModal],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateProjectFormModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});