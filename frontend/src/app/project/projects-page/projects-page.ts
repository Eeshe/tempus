import { Component } from '@angular/core';
import { ProjectList } from '../project-list/project-list';

@Component({
  imports: [ProjectList],
  selector: 'app-projects-page',
  styleUrl: './projects-page.css',
  templateUrl: './projects-page.html',
})
export class ProjectsPage {
}
