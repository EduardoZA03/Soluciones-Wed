import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth.service';

/** Inicio de sesión (HU-01). */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    usuario: ['', Validators.required],
    clave: ['', Validators.required],
  });
  protected readonly error = signal<string | null>(null);
  protected readonly cargando = signal(false);

  ingresar(): void {
    if (this.form.invalid) {
      this.error.set('Usuario y clave son obligatorios');
      return;
    }
    this.error.set(null);
    this.cargando.set(true);
    const { usuario, clave } = this.form.getRawValue();
    this.auth.login(usuario.trim(), clave).subscribe({
      next: () => this.router.navigate(['/inicio']),
      error: (e: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(e.status === 0
          ? 'No se pudo conectar con el servidor. ¿Está encendido el backend?'
          : (e.error?.mensaje ?? 'Error del servidor'));
      },
    });
  }
}
