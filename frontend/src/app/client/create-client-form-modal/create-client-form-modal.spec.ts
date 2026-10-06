import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreateClientFormModal } from './create-client-form-modal';

describe('CreateClientFormModal', () => {
  let component: CreateClientFormModal;
  let fixture: ComponentFixture<CreateClientFormModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateClientFormModal],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateClientFormModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
