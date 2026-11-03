import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

/**
 * Página que se muestra cuando el usuario no tiene permiso para acceder a una ruta.
 */
@Component({
  selector: 'app-forbidden',
  imports: [RouterLink, TranslatePipe],
  templateUrl: './forbidden.component.html',
})
export class ForbiddenComponent {}
