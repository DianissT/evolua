import { Component, input } from '@angular/core';

/**
 * Mensaje de aviso accesible (role="alert") para errores y confirmaciones.
 */
@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
})
export class AlertComponent {
  readonly tipo = input<'danger' | 'success' | 'info' | 'warning'>('danger');
  readonly mensaje = input<string | null>(null);
}
