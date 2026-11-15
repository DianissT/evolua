import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AdminService } from './admin.service';

describe('AdminService', () => {
  let http: HttpTestingController;
  let servicio: AdminService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
    servicio = TestBed.inject(AdminService);
  });

  afterEach(() => http.verify());

  it('lista y crea usuarios', () => {
    servicio.listarUsuarios().subscribe();
    expect(http.expectOne('/api/v1/admin/users').request.method).toBe('GET');

    const nuevo = { nombre: 'Rosa', correo: 'rosa@uni.edu', password: 'Clave2026!', rol: 'DOCENTE' as const };
    servicio.crearUsuario(nuevo).subscribe();
    const crear = http.expectOne('/api/v1/admin/users');
    expect(crear.request.method).toBe('POST');
    expect(crear.request.body).toEqual(nuevo);
  });

  it('cambia el rol y el estado de un usuario', () => {
    servicio.cambiarRol(7, 'ADMIN').subscribe();
    const rol = http.expectOne('/api/v1/admin/users/7/role');
    expect(rol.request.method).toBe('PUT');
    expect(rol.request.body).toEqual({ rol: 'ADMIN' });

    servicio.cambiarEstado(7, false).subscribe();
    const estado = http.expectOne('/api/v1/admin/users/7/active');
    expect(estado.request.body).toEqual({ activo: false });
  });

  it('consulta y actualiza un parámetro de configuración', () => {
    servicio.listarConfiguracion().subscribe();
    expect(http.expectOne('/api/v1/admin/config').request.method).toBe('GET');

    servicio.actualizarConfiguracion('umbral_refuerzo', '70').subscribe();
    const cambio = http.expectOne('/api/v1/admin/config/umbral_refuerzo');
    expect(cambio.request.method).toBe('PUT');
    expect(cambio.request.body).toEqual({ valor: '70' });
  });
});
