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

  readonly availableTasks = computed<Task[]>(() => {
    const projects: Project[] = this.projects();
    const selectedProjectIds: number[] = this.selectedProjects().map((project) => project.id);

    const tasks: Task[] = projects.flatMap((project) => project.tasks);
    const byProject: Task[] = selectedProjectIds.length === 0
      ? tasks
      : tasks.filter((task) => selectedProjectIds.includes(task.projectId));
    return byProject.filter((task) => this.matchesSearch(task.name));
  });

  readonly allSelected = computed<boolean>(() => {
    const available: Task[] = this.availableTasks();

    return available.length > 0 && available.length === this.selectedTasks().length;
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
    this.selectedTasksChangeEvent.emit(this.allSelected() ? [] : [...this.availableTasks()]);
  }
}
