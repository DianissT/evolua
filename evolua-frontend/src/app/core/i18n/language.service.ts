import { Injectable, inject, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

export type Idioma = 'es' | 'en' | 'pt';

/**
 * Gestiona el idioma de la interfaz (español, inglés o portugués) y lo recuerda entre visitas.
 */
@Injectable({ providedIn: 'root' })
export class LanguageService {
  private static readonly CLAVE = 'evolua.idioma';

  private readonly translate = inject(TranslateService);

  readonly idiomas: readonly Idioma[] = ['es', 'en', 'pt'];
  readonly actual = signal<Idioma>('es');

  iniciar(): Observable<unknown> {
    const guardado = localStorage.getItem(LanguageService.CLAVE) as Idioma | null;
    return this.usar(guardado !== null && this.idiomas.includes(guardado) ? guardado : 'es');
  }

  usar(idioma: Idioma): Observable<unknown> {
    this.actual.set(idioma);
    document.documentElement.lang = idioma;
    localStorage.setItem(LanguageService.CLAVE, idioma);
    return this.translate.use(idioma);
  }
}
