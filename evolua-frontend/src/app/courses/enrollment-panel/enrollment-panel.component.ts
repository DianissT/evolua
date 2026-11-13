import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { mensajeDeError } from '../../core/http/error-message';
import { Matricula } from '../../core/models/matricula.model';
import { EnrollmentService } from '../../core/services/enrollment.service';
import { AlertComponent } from '../../shared/alert/alert.component';

/**
 * Panel del docente para consultar, agregar y quitar los estudiantes matriculados en un curso.
 */
@Component({
  selector: 'app-enrollment-panel',
  imports: [FormsModule, ReactiveFormsModule, DatePipe, TranslatePipe, AlertComponent],
  templateUrl: './enrollment-panel.component.html',
})
export class EnrollmentPanelComponent implements OnInit {
  private readonly matriculasApi = inject(EnrollmentService);
  private readonly translate = inject(TranslateService);

  readonly courseId = input.required<number>();

  readonly matriculas = signal<Matricula[]>([]);
  readonly error = signal<string | null>(null);
  readonly exito = signal<string | null>(null);
  readonly enviado = signal(false);
  readonly correo = new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] });

  ngOnInit(): void {
    this.cargar();
  }

  matricular(): void {
    this.enviado.set(true);
    this.exito.set(null);
    this.correo.setValue(this.correo.value.trim());
    if (this.correo.invalid) {
      return;
    }
    this.matriculasApi.matricular(this.courseId(), this.correo.value).subscribe({
      next: (matricula) => {
        this.correo.reset();
        this.enviado.set(false);
        this.error.set(null);
        this.exito.set(this.translate.instant('enrollments.added', { name: matricula.estudianteNombre }) as string);
        this.cargar();
      },
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  quitar(matricula: Matricula): void {
    const mensaje = this.translate.instant('enrollments.confirmRemove', { name: matricula.estudianteNombre }) as string;
    if (!confirm(mensaje)) {
      return;
    }
    this.exito.set(null);
    this.matriculasApi.eliminar(this.courseId(), matricula.estudianteId).subscribe({
      next: () => this.cargar(),
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  private cargar(): void {
    this.matriculasApi.listar(this.courseId()).subscribe({
      next: (matriculas) => this.matriculas.set(matriculas),
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }
}
