import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { AuthService } from '../../core/auth/auth.service';
import { LanguageSwitcherComponent } from '../language-switcher/language-switcher.component';

interface Enlace {
  ruta: string;
  clave: string;
}

/**Estructura común de las pantallas autenticadas: barra de navegación según el rol y contenido principal.*/
@Component({
  selector: 'app-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, TranslatePipe, LanguageSwitcherComponent],
  templateUrl: './layout.component.html',
})
export class LayoutComponent {
  private readonly auth = inject(AuthService);

  readonly usuario = this.auth.usuario;
  readonly menuAbierto = signal(false);
  readonly enlaces = computed<Enlace[]>(() => {
    switch (this.auth.rol()) {
      case 'ADMIN':
        return [{ ruta: '/admin/audit', clave: 'nav.audit' }];
      case 'ESTUDIANTE':
        return [
          { ruta: '/courses', clave: 'nav.courses' },
          { ruta: '/practice', clave: 'nav.practice' },
          { ruta: '/progress', clave: 'nav.progress' },
        ];
      default:
        return [{ ruta: '/courses', clave: 'nav.courses' }];
    }
  });

  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  salir(): void {
    this.auth.salir();
  }
}
