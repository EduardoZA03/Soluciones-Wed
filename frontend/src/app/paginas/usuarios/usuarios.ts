import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../compartido/modal';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { AvisosService } from '../../core/avisos.service';
import { Usuario } from '../../core/modelos';

/** Usuarios y roles (HU-01). */
@Component({
  selector: 'app-usuarios',
  imports: [ReactiveFormsModule, Modal, DatePipe],
  templateUrl: './usuarios.html',
})
export class Usuarios implements OnInit {
  private readonly api = inject(ApiService);
  private readonly avisos = inject(AvisosService);
  protected readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly roles = signal<string[]>([]);
  protected readonly modalNuevo = signal(false);
  protected readonly claveDe = signal<Usuario | null>(null);

  protected readonly form = this.fb.group({
    usuario: ['', [Validators.required, Validators.maxLength(50)]],
    clave: ['', [Validators.required, Validators.minLength(4)]],
    rol: ['Empleado', Validators.required],
  });
  protected readonly formClave = this.fb.group({ clave: ['', [Validators.required, Validators.minLength(4)]] });

  ngOnInit(): void {
    this.cargar();
    this.api.roles().subscribe({ next: (r) => this.roles.set(r), error: () => {} });
  }

  cargar(): void {
    this.api.usuarios().subscribe({ next: (u) => this.usuarios.set(u), error: (e) => this.avisos.errorHttp(e) });
  }

  esYo(u: Usuario): boolean {
    return u.id === this.auth.sesion()?.id;
  }

  nuevo(): void {
    this.form.reset({ usuario: '', clave: '', rol: 'Empleado' });
    this.modalNuevo.set(true);
  }

  crear(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.api.crearUsuario(this.form.getRawValue()).subscribe({
      next: () => { this.avisos.exito('Usuario creado'); this.modalNuevo.set(false); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  cambiarRol(u: Usuario, rol: string): void {
    this.api.actualizarUsuario(u.id, { rol }).subscribe({
      next: () => { this.avisos.exito(`Rol de ${u.usuario}: ${rol}`); this.cargar(); },
      error: (e) => { this.avisos.errorHttp(e); this.cargar(); },
    });
  }

  abrirClave(u: Usuario): void {
    this.formClave.reset();
    this.claveDe.set(u);
  }

  guardarClave(): void {
    const u = this.claveDe();
    if (!u || this.formClave.invalid) {
      this.formClave.markAllAsTouched();
      return;
    }
    this.api.actualizarUsuario(u.id, { clave: this.formClave.getRawValue().clave }).subscribe({
      next: () => { this.avisos.exito('Clave actualizada'); this.claveDe.set(null); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  eliminar(u: Usuario): void {
    if (!confirm(`¿Eliminar al usuario "${u.usuario}"?`)) {
      return;
    }
    this.api.eliminarUsuario(u.id).subscribe({
      next: () => { this.avisos.exito('Usuario eliminado'); this.cargar(); },
      error: (e) => this.avisos.errorHttp(e),
    });
  }
}
