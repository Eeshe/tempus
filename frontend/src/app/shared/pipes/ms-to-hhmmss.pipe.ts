import { Pipe, PipeTransform } from "@angular/core";
import { durationFromMs, formatHHMMSSTime } from "../util/time.util";

@Pipe({
  name: 'msToHHMMSS',
  standalone: true,
})
export class MsToHHMMSSPipe implements PipeTransform {
  transform(value: number | null): string {
    return formatHHMMSSTime(durationFromMs(value ?? 0));
  }
}
