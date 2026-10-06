import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClientModal } from './client-modal';

describe('ClientModal', () => {
  let component: ClientModal;
  let fixture: ComponentFixture<ClientModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientModal],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
