import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { mensajeDeError } from '../../core/http/error-message';
import { CourseService } from '../../core/services/course.service';
import { AlertComponent } from '../../shared/alert/alert.component';

/**
 * Formulario para crear o editar un curso. Si recibe un identificador, edita el curso existente.
 */
@Component({
  selector: 'app-course-form',
  imports: [ReactiveFormsModule, RouterLink, TranslatePipe, AlertComponent],
  templateUrl: './course-form.component.html',
})
export class CourseFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly cursos = inject(CourseService);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);

  readonly id = input<string>();
  readonly idNumerico = computed(() => (this.id() ? Number(this.id()) : null));

  readonly formulario = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: [''],
  });
  readonly enviado = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.idNumerico();
    if (id !== null) {
      this.cursos.obtener(id).subscribe({
        next: (curso) => this.formulario.patchValue({ nombre: curso.nombre, descripcion: curso.descripcion ?? '' }),
        error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
      });
    }
  }

  guardar(): void {
    this.enviado.set(true);
    if (this.formulario.invalid) {
      return;
    }
    const valor = this.formulario.getRawValue();
    const solicitud = { nombre: valor.nombre.trim(), descripcion: valor.descripcion.trim() || null };
    const id = this.idNumerico();
    this.guardando.set(true);
    this.error.set(null);
    const accion = id === null ? this.cursos.crear(solicitud) : this.cursos.actualizar(id, solicitud);
    accion.subscribe({
      next: (curso) => void this.router.navigate(['/courses', curso.id]),
      error: (error: unknown) => {
        this.guardando.set(false);
        this.error.set(mensajeDeError(error, this.translate));
      },
    });
  }
}
