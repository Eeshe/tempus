import { Component, input, output } from '@angular/core';
import { Project } from '../models/project.model';
import { ProjectPopupSelectorBase } from '../project-popup-selector-base';

@Component({
  imports: [],
  selector: 'app-project-selector-button',
  styleUrl: './project-selector-button.css',
  templateUrl: './project-selector-button.html',
})
export class ProjectSelectorButton extends ProjectPopupSelectorBase {
  readonly selectedProject = input<Project | null>();

  readonly projectSelectEvent = output<Project>();

  changeProject(project: Project): void {
    this.projectSelectEvent.emit(project);
    this.toggle();
  }
}
