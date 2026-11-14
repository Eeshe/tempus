import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Client } from '../../../../client/models/client.model';
import { Project } from '../../../../project/models/project.model';
import { Task } from '../../../../task/models/task.model';
import { TaskFilter } from './task-filter';

function createClient(id: number, name: string): Client {
  return { id, name, userId: 1, createdAt: '2024-01-01T00:00:00.000Z' };
}

function createTask(id: number, name: string, projectId: number): Task {
  return { id, name, userId: 1, projectId, createdAt: '2024-01-01T00:00:00.000Z' };
}

function createProject(id: number, name: string, tasks: Task[]): Project {
  return {
    id,
    name,
    userId: 1,
    isPrivate: false,
    isArchived: false,
    hourlyRate: 0,
    color: '#FFFFFF',
    tasks,
    client: createClient(id, `${name} client`),
    createdAt: '2024-01-01T00:00:00.000Z',
  };
}

describe('TaskFilter', () => {
  let component: TaskFilter;
  let fixture: ComponentFixture<TaskFilter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskFilter],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskFilter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('entry ordering', () => {
    const alphaOne: Task = createTask(1, 'One', 1);
    const alphaTwo: Task = createTask(2, 'Two', 1);
    const alphaThree: Task = createTask(3, 'Three', 1);
    const betaOne: Task = createTask(4, 'One', 2);
    const betaTwo: Task = createTask(5, 'Two', 2);

    function renderedNames(): string[] {
      return component.availableTasks().map((task: Task) => task.name);
    }

    beforeEach(() => {
      // Projects and their tasks alphabetical, the way they are loaded on opening the popup.
      component.projects.set([
        createProject(1, 'Alpha', [alphaOne, alphaTwo, alphaThree]),
        createProject(2, 'Beta', [betaOne, betaTwo]),
      ]);
    });

    it('lists the tasks alphabetically grouped by project when nothing is selected', () => {
      expect(renderedNames()).toEqual(['One', 'Two', 'Three', 'One', 'Two']);
    });

    it('lists the selected tasks first inside their own project, keeping the project order', () => {
      fixture.componentRef.setInput('selectedTasks', [alphaThree, betaTwo]);

      expect(renderedNames()).toEqual(['Three', 'One', 'Two', 'Two', 'One']);
      expect(
        component.availableTasks().map((task: Task) => task.projectId),
      ).toEqual([1, 1, 1, 2, 2]);
    });

    it('keeps selected tasks pinned in their project when they do not match the search', () => {
      fixture.componentRef.setInput('selectedTasks', [alphaThree, betaTwo]);
      component.searchQuery.set('tw');

      expect(renderedNames()).toEqual(['Three', 'Two', 'Two']);
    });

    it('scopes the list to the selected projects', () => {
      fixture.componentRef.setInput('selectedTasks', [alphaThree]);
      fixture.componentRef.setInput('selectedProjects', [{ ...createProject(2, 'Beta', []) }]);

      expect(renderedNames()).toEqual(['One', 'Two']);
    });

    it('selects only the search matches with Select All', () => {
      const emitted: Task[][] = [];
      component.selectedTasksChangeEvent.subscribe((tasks: Task[]) => emitted.push(tasks));
      fixture.componentRef.setInput('selectedTasks', [alphaThree]);
      component.searchQuery.set('tw');

      component.toggleSelectAll();

      expect(emitted).toEqual([[alphaTwo, betaTwo]]);
    });

    it('reports all entries selected only when every search match is selected', () => {
      fixture.componentRef.setInput('selectedTasks', [alphaThree]);

      expect(component.allSelected()).toBe(false);

      fixture.componentRef.setInput('selectedTasks', [
        alphaOne,
        alphaTwo,
        alphaThree,
        betaOne,
        betaTwo,
      ]);

      expect(component.allSelected()).toBe(true);
    });
  });
});
