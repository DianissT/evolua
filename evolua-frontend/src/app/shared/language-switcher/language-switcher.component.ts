import { Component, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

import { Idioma, LanguageService } from '../../core/i18n/language.service';

/**
 * Selector de idioma de la interfaz.
 */
@Component({
  selector: 'app-language-switcher',
  imports: [TranslatePipe],
  templateUrl: './language-switcher.component.html',
})
export class LanguageSwitcherComponent {
  private readonly idioma = inject(LanguageService);

  readonly idiomas = this.idioma.idiomas;
  readonly actual = this.idioma.actual;

  cambiar(idioma: Idioma): void {
    this.idioma.usar(idioma).subscribe();
  }
}
