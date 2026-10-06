import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClientProjectList } from './client-project-list';

describe('ClientProjectList', () => {
  let component: ClientProjectList;
  let fixture: ComponentFixture<ClientProjectList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientProjectList],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientProjectList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
