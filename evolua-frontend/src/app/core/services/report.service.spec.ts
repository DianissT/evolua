import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ReportService } from './report.service';

describe('ReportService', () => {
  let http: HttpTestingController;
  let servicio: ReportService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
    servicio = TestBed.inject(ReportService);
  });

  afterEach(() => http.verify());

  it('pide el rendimiento de un curso', () => {
    servicio.rendimiento(4).subscribe();
    expect(http.expectOne('/api/v1/reports/courses/4/performance').request.method).toBe('GET');
  });

  it('pide las preguntas difíciles con y sin filtro de tema', () => {
    servicio.preguntasDificiles(4).subscribe();
    const sinFiltro = http.expectOne('/api/v1/reports/courses/4/hard-questions');
    expect(sinFiltro.request.params.has('temaId')).toBe(false);
    sinFiltro.flush([]);

    servicio.preguntasDificiles(4, 9).subscribe();
    const conFiltro = http.expectOne((req) => req.url === '/api/v1/reports/courses/4/hard-questions');
    expect(conFiltro.request.params.get('temaId')).toBe('9');
  });
});
