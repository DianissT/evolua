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
            path: ':courseId/quizzes/new',
            canActivate: [rolGuard('DOCENTE')],
            loadComponent: () =>
              import('./quizzes/quiz-form/quiz-form.component').then((m) => m.QuizFormComponent),
          },
          {
            path: ':courseId/quizzes/:quizId/edit',
            canActivate: [rolGuard('DOCENTE')],
            loadComponent: () =>
              import('./quizzes/quiz-form/quiz-form.component').then((m) => m.QuizFormComponent),
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
        path: 'attempts/:attemptId',
        canActivate: [rolGuard('ESTUDIANTE')],
        loadComponent: () => import('./quizzes/attempt/attempt.component').then((m) => m.AttemptComponent),
      },
      {
        path: 'attempts/:attemptId/result',
        canActivate: [rolGuard('ESTUDIANTE')],
        loadComponent: () =>
          import('./quizzes/attempt-result/attempt-result.component').then((m) => m.AttemptResultComponent),
      },
      {
        path: 'practice',
        canActivate: [rolGuard('ESTUDIANTE')],
        loadComponent: () =>
          import('./practice/practice-start/practice-start.component').then((m) => m.PracticeStartComponent),
      },
      {
        path: 'progress',
        canActivate: [rolGuard('ESTUDIANTE')],
        loadComponent: () =>
          import('./progress/my-progress/my-progress.component').then((m) => m.MyProgressComponent),
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
