import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Client } from '../models/client.model';
import { ClientProjectList } from './client-project-list';

describe('ClientProjectList', () => {
  let component: ClientProjectList;
  let fixture: ComponentFixture<ClientProjectList>;

  const mockClient: Client = {
    id: 1,
    name: 'Tempus',
    userId: 1,
    createdAt: '2026-01-01T00:00:00Z',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientProjectList],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientProjectList);
    fixture.componentRef.setInput('client', mockClient);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
