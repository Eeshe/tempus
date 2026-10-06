export interface Duration {
  hours: number;
  minutes: number;
  seconds: number;
  totalMilliseconds: number;
}

export function computeDuration(
  start: Date | string | null | undefined,
  end: Date | string | null | undefined
): Duration | null {
  if (start == null) return null;
  if (end == null) {
    end = new Date();
  }
  const startMs = new Date(start).getTime();
  const endMs = new Date(end).getTime();
  if (Number.isNaN(startMs)) {
    return null;
  }
  if (Number.isNaN(endMs)) {
    return null;
  }

  return durationFromMs(endMs - startMs);
}

export function durationFromMs(totalMilliseconds: number): Duration {
  const ms = Math.max(0, totalMilliseconds);
  const totalSeconds = Math.floor(ms / 1000);

  return {
    hours: Math.floor(totalSeconds / 3600),
    minutes: Math.floor((totalSeconds % 3600) / 60),
    seconds: totalSeconds % 60,
    totalMilliseconds: ms,
  };
}

export function formatHHMMSSTime(duration: Duration | null): string {
  if (duration == null) {
    return "";
  }
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${pad(duration.hours)}:${pad(duration.minutes)}:${pad(duration.seconds)}`;
}

export function toHHmmTime(date: Date): string {
  const pad = (n: number) => n.toString().padStart(2, '0');
  return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function fromHHmmTime(time: string, base: Date): Date {
  const [hours, minutes] = time.split(':').map(Number);
  const result = new Date(base);          // clone so we don't mutate the input
  result.setHours(hours, minutes, 0, 0);  // reset seconds/ms to 0
  return result;
}

export function HHmmToMinutes(HHmm: string): number {
  const [hours, minutes] = HHmm.split(':').map(Number);
  return hours * 60 + minutes;
}
