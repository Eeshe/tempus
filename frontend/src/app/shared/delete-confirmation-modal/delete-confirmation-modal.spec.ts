import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DeleteConfirmationModal } from './delete-confirmation-modal';

describe('DeleteConfirmationModal', () => {
  let component: DeleteConfirmationModal;
  let fixture: ComponentFixture<DeleteConfirmationModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteConfirmationModal],
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteConfirmationModal);
    fixture.componentRef.setInput('toDeleteName', 'Test');
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
