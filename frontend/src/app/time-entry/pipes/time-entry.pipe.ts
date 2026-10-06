import { Pipe, PipeTransform } from "@angular/core";
import { TimeEntry } from "../models/time-entry.model";

@Pipe({
  name: "displayName",
  standalone: true,
})
export class DisplayNamePipe implements PipeTransform {
  transform(timeEntry: TimeEntry) {
    return timeEntry.project.name + (timeEntry.task ? ":" + timeEntry.task.name : "")
  }
}
