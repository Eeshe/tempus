import { ChangeDetectionStrategy, Component } from '@angular/core';
import { NewTimeEntryButton } from '../time-entry/new-time-entry/new-time-entry-button/new-time-entry-button';
import { TimeEntryList } from '../time-entry/time-entry-list/time-entry-list';

@Component({
  selector: 'app-home',
  imports: [TimeEntryList, NewTimeEntryButton],
  templateUrl: './home.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './home.css',
})
export class Home {

}
