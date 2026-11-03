import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controlador: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpClient);
    controlador = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => controlador.verify());

  it('agrega el token a las solicitudes de la API', () => {
    vi.spyOn(auth, 'token').mockReturnValue('abc');
    http.get('/api/v1/courses').subscribe();
    const solicitud = controlador.expectOne('/api/v1/courses');
    expect(solicitud.request.headers.get('Authorization')).toBe('Bearer abc');
    solicitud.flush([]);
  });

  it('no agrega el token a otras direcciones', () => {
    vi.spyOn(auth, 'token').mockReturnValue('abc');
    http.get('/i18n/es.json').subscribe();
    const solicitud = controlador.expectOne('/i18n/es.json');
    expect(solicitud.request.headers.has('Authorization')).toBe(false);
    solicitud.flush({});
  });

  it('cierra la sesión y redirige al login ante un 401', () => {
    const cerrar = vi.spyOn(auth, 'cerrarSesion');
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    http.get('/api/v1/courses').subscribe({ error: () => undefined });
    controlador.expectOne('/api/v1/courses').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(cerrar).toHaveBeenCalled();
    expect(navegar).toHaveBeenCalledWith(['/login'], { queryParams: { sesion: 'expirada' } });
  });

  it('no cierra la sesión cuando el 401 viene del inicio de sesión', () => {
    const cerrar = vi.spyOn(auth, 'cerrarSesion');
    http.post('/api/v1/auth/login', {}).subscribe({ error: () => undefined });
    controlador.expectOne('/api/v1/auth/login').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(cerrar).not.toHaveBeenCalled();
  });
});
