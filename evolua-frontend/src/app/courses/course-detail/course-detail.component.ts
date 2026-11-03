import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { mensajeDeError } from '../../core/http/error-message';
import { Curso, Tema } from '../../core/models/curso.model';
import { CourseService } from '../../core/services/course.service';
import { TopicService } from '../../core/services/topic.service';
import { AlertComponent } from '../../shared/alert/alert.component';

/**
 * Detalle de un curso y sus temas. El docente administra los temas; el estudiante solo los consulta.
 */
@Component({
  selector: 'app-course-detail',
  imports: [FormsModule, ReactiveFormsModule, RouterLink, TranslatePipe, AlertComponent],
  templateUrl: './course-detail.component.html',
})
export class CourseDetailComponent implements OnInit {
  private readonly cursos = inject(CourseService);
  private readonly temasApi = inject(TopicService);
  private readonly auth = inject(AuthService);
  private readonly translate = inject(TranslateService);

  readonly id = input.required<string>();
  readonly idNumerico = computed(() => Number(this.id()));

  readonly curso = signal<Curso | null>(null);
  readonly temas = signal<Tema[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly esDocente = computed(() => this.auth.rol() === 'DOCENTE');
  readonly editando = signal<number | null>(null);

  readonly nuevoTema = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(120)],
  });
  readonly nombreEdicion = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(120)],
  });
  readonly nuevoEnviado = signal(false);

  ngOnInit(): void {
    this.cursos.obtener(this.idNumerico()).subscribe({
      next: (curso) => {
        this.curso.set(curso);
        this.cargarTemas();
      },
      error: (error: unknown) => this.fallar(error),
    });
  }


  agregarTema(): void {
  this.nuevoEnviado.set(true);

  if (this.nuevoTema.invalid) {
    return;
  }

  this.temasApi.crear({
    cursoId: this.idNumerico(),
    nombre: this.nuevoTema.value.trim()
  }).subscribe({
    next: () => {
      this.nuevoTema.reset();
      this.nuevoEnviado.set(false);
      this.cargarTemas();
    },
    error: (error: unknown) =>
      this.error.set(mensajeDeError(error, this.translate)),
  });
}

  editar(tema: Tema): void {
    this.nombreEdicion.setValue(tema.nombre);
    this.editando.set(tema.id);
  }

  cancelarEdicion(): void {
    this.editando.set(null);
  }

  guardarEdicion(tema: Tema): void {
    if (this.nombreEdicion.invalid) {
      return;
    }
    this.temasApi
      .actualizar(tema.id, { cursoId: tema.cursoId, nombre: this.nombreEdicion.value.trim(), orden: tema.orden })
      .subscribe({
        next: () => {
          this.editando.set(null);
          this.cargarTemas();
        },
        error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
      });
  }

  alternarActivo(tema: Tema): void {
    if (tema.activo) {
      const mensaje = this.translate.instant('topics.confirmDeactivate', { name: tema.nombre }) as string;
      if (!confirm(mensaje)) {
        return;
      }
    }
    const accion: Observable<unknown> = tema.activo
      ? this.temasApi.desactivar(tema.id)
      : this.temasApi.actualizar(tema.id, {
          cursoId: tema.cursoId,
          nombre: tema.nombre,
          orden: tema.orden,
          activo: true,
        });
    accion.subscribe({
      next: () => this.cargarTemas(),
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  private cargarTemas(): void {
    this.cursos.temas(this.idNumerico()).subscribe({
      next: (temas) => {
        this.temas.set(temas);
        this.error.set(null);
        this.cargando.set(false);
      },
      error: (error: unknown) => this.fallar(error),
    });
  }

  private fallar(error: unknown): void {
    this.error.set(mensajeDeError(error, this.translate));
    this.cargando.set(false);
  }
}
