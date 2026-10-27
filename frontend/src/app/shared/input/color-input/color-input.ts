import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { ColorPickerDirective } from 'ngx-color-picker';
import { DEFAULT_PROJECT_COLOR } from '../../../project/models/project.model';

const PRESET_PROJECT_COLORS: string[] = [
  '#FFFFFF',
  '#9AA3B2',
  '#F2555C',
  '#FF9F45',
  '#FFD166',
  '#3DD68C',
  '#2EC4B6',
  '#4D9DE0',
  '#86b9e4',
  '#7B61FF',
  '#C77DFF',
  '#7028a5',
];

@Component({
  imports: [ColorPickerDirective],
  selector: 'app-color-input',
  styleUrl: './color-input.css',
  templateUrl: './color-input.html',
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class ColorInput {
  readonly id = input<string>('');
  readonly value = input<string>(DEFAULT_PROJECT_COLOR);
  readonly label = input<string>('Color');

  readonly valueChange = output<string>();

  readonly presetColors: string[] = PRESET_PROJECT_COLORS;

  commitColor(color: string): void {
    const nextColor: string | null = color?.trim() || this.value() || DEFAULT_PROJECT_COLOR;
    if (nextColor === this.value()) {
      return;
    }
    this.valueChange.emit(nextColor);
  }
}
