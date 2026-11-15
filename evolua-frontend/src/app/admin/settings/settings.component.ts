import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormsModule,ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { mensajeDeError } from '../../core/http/error-message';
import { AdminService } from '../../core/services/admin.service';
import { AlertComponent } from '../../shared/alert/alert.component';

interface Limites {
  min: number;
  max: number;
}

interface Fila {
  clave: string;
  guardado: string;
  control: FormControl<string>;
  limites: Limites | null;
  intentado: boolean;
}

/** Rango permitido de cada parámetro; el servidor aplica las mismas reglas (ValidadorConfiguracion). */
const LIMITES: Record<string, Limites> = {
  umbral_refuerzo: { min: 0, max: 99 },
  umbral_dominio: { min: 1, max: 100 },
  min_respuestas_tema: { min: 1, max: 100 },
  intentos_para_dominio: { min: 1, max: 10 },
  preguntas_por_practica: { min: 1, max: 20 },
};

/**
 * Edición de los umbrales del motor adaptativo y de otros parámetros del sistema. Disponible solo para
 * administradores. El servidor valida cada cambio y lo registra en la auditoría.
 */
@Component({
  selector: 'app-settings',
  imports: [ReactiveFormsModule, FormsModule, TranslatePipe, AlertComponent],
  templateUrl: './settings.component.html',
})
export class SettingsComponent implements OnInit {
  private readonly api = inject(AdminService);
  private readonly translate = inject(TranslateService);

  readonly filas = signal<Fila[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly exito = signal<string | null>(null);

  ngOnInit(): void {
    this.api.listarConfiguracion().subscribe({
      next: (parametros) => {
        this.filas.set(parametros.map((p) => this.crearFila(p.clave, p.valor)));
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        this.error.set(mensajeDeError(error, this.translate));
        this.cargando.set(false);
      },
    });
  }

  guardar(fila: Fila): void {
    fila.intentado = true;
    this.exito.set(null);
    this.filas.update((filas) => [...filas]);
    if (fila.control.invalid) {
      return;
    }
    this.api.actualizarConfiguracion(fila.clave, fila.control.value.trim()).subscribe({
      next: (parametro) => {
        fila.guardado = parametro.valor;
        fila.control.setValue(parametro.valor);
        fila.intentado = false;
        this.error.set(null);
        this.exito.set(this.translate.instant('settings.saved') as string);
        this.filas.update((filas) => [...filas]);
      },
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  restaurar(fila: Fila): void {
    fila.control.setValue(fila.guardado);
    fila.intentado = false;
    this.filas.update((filas) => [...filas]);
  }

  private crearFila(clave: string, valor: string): Fila {
    const limites = LIMITES[clave] ?? null;
    const validadores = [Validators.required, Validators.pattern(/^\d+$/)];
    if (limites !== null) {
      validadores.push(Validators.min(limites.min), Validators.max(limites.max));
    }
    return {
      clave,
      guardado: valor,
      control: new FormControl(valor, { nonNullable: true, validators: validadores }),
      limites,
      intentado: false,
    };
  }
}
