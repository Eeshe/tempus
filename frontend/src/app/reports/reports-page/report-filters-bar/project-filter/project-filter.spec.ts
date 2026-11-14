import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Client } from '../../../../client/models/client.model';
import { Project } from '../../../../project/models/project.model';
import { ProjectFilter } from './project-filter';

function createProject(id: number, name: string): Project {
  return {
    id,
    name,
    userId: 1,
    isPrivate: false,
    isArchived: false,
    hourlyRate: 0,
    color: '#FFFFFF',
    tasks: [],
    client: createClient(id, `${name} client`),
    createdAt: '2024-01-01T00:00:00.000Z',
  };
}

function createClient(id: number, name: string): Client {
  return { id, name, userId: 1, createdAt: '2024-01-01T00:00:00.000Z' };
}

describe('ProjectFilter', () => {
  let component: ProjectFilter;
  let fixture: ComponentFixture<ProjectFilter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectFilter],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectFilter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('entry ordering', () => {
    const alpha: Project = createProject(1, 'Alpha');
    const beta: Project = createProject(2, 'Beta');
    const gamma: Project = createProject(3, 'Gamma');

    function renderedNames(): string[] {
      return component.filteredProjects().map((project: Project) => project.name);
    }

    beforeEach(() => {
      // Alphabetical, the way the project service hands them over.
      component.projects.set([alpha, beta, gamma]);
    });

    it('lists the projects alphabetically when nothing is selected', () => {
      expect(renderedNames()).toEqual(['Alpha', 'Beta', 'Gamma']);
    });

    it('lists the selected projects first, alphabetically among themselves', () => {
      fixture.componentRef.setInput('selectedProjects', [gamma, alpha]);

      expect(renderedNames()).toEqual(['Alpha', 'Gamma', 'Beta']);
    });

    it('keeps selected projects pinned on top even when they do not match the search', () => {
      fixture.componentRef.setInput('selectedProjects', [gamma]);
      component.searchQuery.set('alp');

      expect(renderedNames()).toEqual(['Gamma', 'Alpha']);
    });

    it('selects only the search matches with Select All', () => {
      const emitted: Project[][] = [];
      component.selectedProjectsChangeEvent.subscribe((projects: Project[]) =>
        emitted.push(projects)
      );
      fixture.componentRef.setInput('selectedProjects', [gamma]);
      component.searchQuery.set('alp');

      component.toggleSelectAll();

      expect(emitted).toEqual([[alpha]]);
    });

    it('reports all entries selected only when every search match is selected', () => {
      fixture.componentRef.setInput('selectedProjects', [gamma]);

      expect(component.allSelected()).toBe(false);

      fixture.componentRef.setInput('selectedProjects', [alpha, beta, gamma]);

      expect(component.allSelected()).toBe(true);
    });
  });
});
