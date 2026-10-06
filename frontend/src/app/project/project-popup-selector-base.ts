import { Directive, inject, signal } from '@angular/core';
import { ProjectService } from '../services/project.service';
import { PopupSelectorBase } from '../shared/selector/popup-selector-base';
import { Project } from './models/project.model';

@Directive({
  standalone: true,
})
export abstract class ProjectPopupSelectorBase extends PopupSelectorBase {
  private readonly projectService: ProjectService = inject(ProjectService);

  readonly projects = signal<Project[]>([]);

  protected override openPopup(): void {
    super.openPopup();

    this.projectService.listProjects().subscribe((fetchedProjects) => {
      const visibleProjects: Project[] = this.filterProjects(fetchedProjects);
      this.projects.set(
        visibleProjects.sort((a, b) => a.name.localeCompare(b.name, undefined, { sensitivity: 'base' }))
      );
    });
  }

  protected filterProjects(projects: Project[]): Project[] {
    return projects;
  }
}
