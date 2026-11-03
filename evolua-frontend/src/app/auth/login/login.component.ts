import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { rutaInicial } from '../../core/auth/auth.guard';
import { AuthService } from '../../core/auth/auth.service';
import { mensajeDeError } from '../../core/http/error-message';
import { AlertComponent } from '../../shared/alert/alert.component';
import { LanguageSwitcherComponent } from '../../shared/language-switcher/language-switcher.component';

/**
 * Pantalla de inicio de sesión.
 */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, TranslatePipe, AlertComponent, LanguageSwitcherComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly translate = inject(TranslateService);

  readonly formulario = this.fb.group({
    correo: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });
  readonly enviado = signal(false);
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly sesionExpirada = this.route.snapshot.queryParamMap.get('sesion') === 'expirada';

  ingresar(): void {
    this.enviado.set(true);
    if (this.formulario.invalid) {
      return;
    }
    this.cargando.set(true);
    this.error.set(null);
    this.auth.login(this.formulario.getRawValue()).subscribe({
      next: (usuario) => {
        const destino = this.route.snapshot.queryParamMap.get('returnUrl');
        const esInterno = destino !== null && destino.startsWith('/') && !destino.startsWith('//');
        void this.router.navigateByUrl(esInterno ? destino : rutaInicial(usuario.rol));
      },
      error: (error: unknown) => {
        this.cargando.set(false);
        this.error.set(
          error instanceof HttpErrorResponse && error.status === 401
            ? (this.translate.instant('login.invalid') as string)
            : mensajeDeError(error, this.translate),
        );
      },
    });
  }
}
