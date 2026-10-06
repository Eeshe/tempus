import { Pipe, PipeTransform } from "@angular/core";
import { computeDuration, formatHHMMSSTime } from "../util/time.util";

@Pipe({
  name: 'duration',
  standalone: true,
})
export class DurationPipe implements PipeTransform {
  transform(start: Date | string | null, end: Date | string | null): string {
    return formatHHMMSSTime(computeDuration(start, end));
  }
}
