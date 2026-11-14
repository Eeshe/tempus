import { Component, computed, inject, input, output, signal } from '@angular/core';
import { Project } from '../../../../project/models/project.model';
import { ProjectService } from '../../../../services/project.service';
import { SearchInput } from '../../../../shared/input/search-input/search-input';
import { PopupSelectorBase } from '../../../../shared/selector/popup-selector-base';
import { Task } from '../../../../task/models/task.model';

@Component({
  imports: [SearchInput],
  selector: 'app-task-filter',
  styleUrl: './task-filter.css',
  templateUrl: './task-filter.html',
})
export class TaskFilter extends PopupSelectorBase {
  private readonly projectService = inject(ProjectService);

  readonly selectedTasks = input<Task[]>([]);
  readonly selectedProjects = input<Project[]>([]);

  readonly selectedTasksChangeEvent = output<Task[]>();

  readonly projects = signal<Project[]>([]);

  private readonly scopedProjects = computed<Project[]>(() => {
    const selectedProjectIds: number[] = this.selectedProjects().map((project) => project.id);
    const projects: Project[] = this.projects();

    return selectedProjectIds.length === 0
      ? projects
      : projects.filter((project) => selectedProjectIds.includes(project.id));
  });

  readonly matchingTasks = computed<Task[]>(() =>
    this.scopedProjects()
      .flatMap((project) => project.tasks)
      .filter((task) => this.matchesSearch(task.name))
  );

  readonly availableTasks = computed<Task[]>(() =>
    this.scopedProjects().flatMap((project) =>
      this.sortSelectedFirst(
        project.tasks.filter((task) => this.isSelected(task) || this.matchesSearch(task.name)),
        (task) => this.isSelected(task)
      )
    )
  );

  readonly allSelected = computed<boolean>(() => {
    const matches: Task[] = this.matchingTasks();

    return matches.length > 0 && matches.every((task) => this.isSelected(task));
  });

  protected override openPopup(): void {
    super.openPopup();

    this.projectService.listProjects().subscribe(
      (fetchedProjects) =>
        this.projects.set(
          fetchedProjects
            .sort((a, b) => a.name.localeCompare(b.name, undefined, { sensitivity: 'base' }))
            .map((project) => ({
              ...project,
              tasks: [...project.tasks].sort((a, b) =>
                a.name.localeCompare(b.name, undefined, { sensitivity: 'base' })
              ),
            }))
        )
    );
  }

  isSelected(checkTask: Task): boolean {
    return this.selectedTasks().some((task) => task.id === checkTask.id);
  }

  toggleTask(taskToggle: Task): void {
    const current: Task[] = this.selectedTasks();
    const updated: Task[] = this.isSelected(taskToggle)
      ? current.filter((task) => task.id !== taskToggle.id)
      : [...current, taskToggle];

    this.selectedTasksChangeEvent.emit(updated);
  }

  toggleSelectAll(): void {
    this.selectedTasksChangeEvent.emit(this.allSelected() ? [] : [...this.matchingTasks()]);
  }
}
