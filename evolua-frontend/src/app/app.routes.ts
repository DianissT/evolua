import { Routes } from '@angular/router';

import { authGuard, guestGuard, inicioGuard, rolGuard } from './core/auth/auth.guard';
import { LayoutComponent } from './shared/layout/layout.component';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', canActivate: [inicioGuard], children: [] },
      {
        path: 'courses',
        canActivate: [rolGuard('DOCENTE', 'ESTUDIANTE')],
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./courses/course-list/course-list.component').then((m) => m.CourseListComponent),
          },
          {
            path: 'new',
            canActivate: [rolGuard('DOCENTE')],
            loadComponent: () =>
              import('./courses/course-form/course-form.component').then((m) => m.CourseFormComponent),
          },
          {
            path: ':id/edit',
            canActivate: [rolGuard('DOCENTE')],
            loadComponent: () =>
              import('./courses/course-form/course-form.component').then((m) => m.CourseFormComponent),
          },
          {
            path: ':id',
            loadComponent: () =>
              import('./courses/course-detail/course-detail.component').then((m) => m.CourseDetailComponent),
          },
          {
            path: ':courseId/topics/:topicId/questions',
            canActivate: [rolGuard('DOCENTE')],
            loadComponent: () =>
              import('./questions/question-list/question-list.component').then((m) => m.QuestionListComponent),
          },
          {
            path: ':courseId/topics/:topicId/questions/new',
            canActivate: [rolGuard('DOCENTE')],
            loadComponent: () =>
              import('./questions/question-form/question-form.component').then((m) => m.QuestionFormComponent),
          },
          {
            path: ':courseId/topics/:topicId/questions/:questionId/edit',
            canActivate: [rolGuard('DOCENTE')],
            loadComponent: () =>
              import('./questions/question-form/question-form.component').then((m) => m.QuestionFormComponent),
          },
        ],
      },
      {
        path: 'admin/audit',
        canActivate: [rolGuard('ADMIN')],
        loadComponent: () => import('./admin/audit-log/audit-log.component').then((m) => m.AuditLogComponent),
      },
      {
        path: 'forbidden',
        loadComponent: () => import('./errors/forbidden/forbidden.component').then((m) => m.ForbiddenComponent),
      },
    ],
  },
  {
    path: '**',
    loadComponent: () => import('./errors/not-found/not-found.component').then((m) => m.NotFoundComponent),
  },
];
