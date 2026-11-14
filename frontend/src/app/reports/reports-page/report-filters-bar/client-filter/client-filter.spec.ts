import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Client } from '../../../../client/models/client.model';
import { ClientFilter } from './client-filter';

function createClient(id: number, name: string): Client {
  return { id, name, userId: 1, createdAt: '2024-01-01T00:00:00.000Z' };
}

describe('ClientFilter', () => {
  let component: ClientFilter;
  let fixture: ComponentFixture<ClientFilter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientFilter],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientFilter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('entry ordering', () => {
    const acme: Client = createClient(1, 'Acme');
    const globex: Client = createClient(2, 'Globex');
    const initech: Client = createClient(3, 'Initech');

    function renderedNames(): string[] {
      return component.filteredClients().map((client: Client) => client.name);
    }

    beforeEach(() => {
      // Alphabetical, the way the client service hands them over.
      component.clients.set([acme, globex, initech]);
    });

    it('lists the clients alphabetically when nothing is selected', () => {
      expect(renderedNames()).toEqual(['Acme', 'Globex', 'Initech']);
    });

    it('lists the selected clients first, alphabetically among themselves', () => {
      fixture.componentRef.setInput('selectedClients', [initech, acme]);

      expect(renderedNames()).toEqual(['Acme', 'Initech', 'Globex']);
    });

    it('keeps selected clients pinned on top even when they do not match the search', () => {
      fixture.componentRef.setInput('selectedClients', [initech]);
      component.searchQuery.set('ac');

      expect(renderedNames()).toEqual(['Initech', 'Acme']);
    });

    it('selects only the search matches with Select All', () => {
      const emitted: Client[][] = [];
      component.selectedClientsChangeEvent.subscribe((clients: Client[]) => emitted.push(clients));
      fixture.componentRef.setInput('selectedClients', [initech]);
      component.searchQuery.set('ac');

      component.toggleSelectAll();

      expect(emitted).toEqual([[acme]]);
    });

    it('reports all entries selected only when every search match is selected', () => {
      fixture.componentRef.setInput('selectedClients', [initech]);

      expect(component.allSelected()).toBe(false);

      fixture.componentRef.setInput('selectedClients', [acme, globex, initech]);

      expect(component.allSelected()).toBe(true);
    });
  });
});
