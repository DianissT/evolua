import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { mensajeDeError } from '../../core/http/error-message';
import { Curso } from '../../core/models/curso.model';
import { CourseService } from '../../core/services/course.service';
import { AlertComponent } from '../../shared/alert/alert.component';

/**
 * Lista de cursos: los del docente o los cursos activos en los que está matriculado el estudiante.
 */
@Component({
  selector: 'app-course-list',
  imports: [RouterLink, TranslatePipe, AlertComponent],
  templateUrl: './course-list.component.html',
})
export class CourseListComponent implements OnInit {
  private readonly cursos = inject(CourseService);
  private readonly auth = inject(AuthService);
  private readonly translate = inject(TranslateService);

  readonly lista = signal<Curso[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly esDocente = computed(() => this.auth.rol() === 'DOCENTE');

  ngOnInit(): void {
    this.cargar();
  }

  alternarActivo(curso: Curso): void {
    if (curso.activo) {
      const mensaje = this.translate.instant('courses.confirmDeactivate', { name: curso.nombre }) as string;
      if (!confirm(mensaje)) {
        return;
      }
    }
    const accion: Observable<unknown> = curso.activo
      ? this.cursos.desactivar(curso.id)
      : this.cursos.actualizar(curso.id, { nombre: curso.nombre, descripcion: curso.descripcion, activo: true });
    accion.subscribe({
      next: () => this.cargar(),
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  private cargar(): void {
    this.cargando.set(true);
    this.cursos.listar().subscribe({
      next: (cursos) => {
        this.lista.set(cursos);
        this.error.set(null);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        this.error.set(mensajeDeError(error, this.translate));
        this.cargando.set(false);
      },
    });
  }
}
