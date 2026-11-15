import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { AuthService } from '../../core/auth/auth.service';
import { mensajeDeError } from '../../core/http/error-message';
import { UsuarioAdmin } from '../../core/models/admin.model';
import { Rol } from '../../core/models/usuario.model';
import { AdminService } from '../../core/services/admin.service';
import { AlertComponent } from '../../shared/alert/alert.component';

/**
 * Gestión de usuarios y roles, los usuarios no se eliminan: se desactivan, y
 * un administrador no puede cambiar su propio rol ni desactivar su propia cuenta.
 */
@Component({
  selector: 'app-user-admin',
  imports: [DatePipe, ReactiveFormsModule, TranslatePipe, AlertComponent],
  templateUrl: './user-admin.component.html',
})
export class UserAdminComponent implements OnInit {
  private readonly api = inject(AdminService);
  private readonly auth = inject(AuthService);
  private readonly translate = inject(TranslateService);

  readonly roles: readonly Rol[] = ['ESTUDIANTE', 'DOCENTE', 'ADMIN'];
  readonly usuarios = signal<UsuarioAdmin[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly exito = signal<string | null>(null);
  readonly enviado = signal(false);

  readonly formulario = new FormGroup({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(100)] }),
    correo: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email, Validators.maxLength(150)],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(8), Validators.maxLength(72)],
    }),
    rol: new FormControl<Rol>('ESTUDIANTE', { nonNullable: true }),
  });

  ngOnInit(): void {
    this.cargar();
  }

  esYo(usuario: UsuarioAdmin): boolean {
    return this.auth.usuario()?.id === usuario.id;
  }

  crear(): void {
    this.enviado.set(true);
    this.exito.set(null);
    if (this.formulario.invalid) {
      return;
    }
    const valores = this.formulario.getRawValue();
    this.api.crearUsuario({ ...valores, nombre: valores.nombre.trim(), correo: valores.correo.trim() }).subscribe({
      next: (usuario) => {
        this.formulario.reset({ nombre: '', correo: '', password: '', rol: 'ESTUDIANTE' });
        this.enviado.set(false);
        this.error.set(null);
        this.exito.set(this.translate.instant('users.created', { name: usuario.nombre }) as string);
        this.cargar();
      },
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  cambiarRol(usuario: UsuarioAdmin, evento: Event): void {
    const lista = evento.target as HTMLSelectElement;
    const nuevo = lista.value as Rol;
    if (nuevo === usuario.rol) {
      return;
    }
    const rol = this.translate.instant(`roles.${nuevo}`) as string;
    const mensaje = this.translate.instant('users.confirmRole', { name: usuario.nombre, role: rol }) as string;
    if (!confirm(mensaje)) {
      lista.value = usuario.rol;
      return;
    }
    this.exito.set(null);
    this.api.cambiarRol(usuario.id, nuevo).subscribe({
      next: () => {
        this.error.set(null);
        this.exito.set(this.translate.instant('users.roleChanged', { name: usuario.nombre, role: rol }) as string);
        this.cargar();
      },
      error: (error: unknown) => {
        lista.value = usuario.rol;
        this.error.set(mensajeDeError(error, this.translate));
      },
    });
  }

  alternarEstado(usuario: UsuarioAdmin): void {
    this.exito.set(null);
    this.api.cambiarEstado(usuario.id, !usuario.activo).subscribe({
      next: () => {
        this.error.set(null);
        this.cargar();
      },
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  private cargar(): void {
    this.api.listarUsuarios().subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        this.error.set(mensajeDeError(error, this.translate));
        this.cargando.set(false);
      },
    });
  }
}
