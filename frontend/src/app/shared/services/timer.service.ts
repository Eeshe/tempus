import { Service } from "@angular/core";
import { Observable, shareReplay, timer } from "rxjs";

@Service()
export class TimerService {
  readonly oneSecondTick$: Observable<number> = timer(0, 1000).pipe(shareReplay(1));
}
