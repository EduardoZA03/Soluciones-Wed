import { Component, OnDestroy, OnInit, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Html5Qrcode } from 'html5-qrcode';
import { ApiService } from '../../core/api.service';
import { AvisosService } from '../../core/avisos.service';
import { OrdenCompra, tono } from '../../core/modelos';

/**
 * Recepción de insumos por QR o código de barras (HU-08).
 * El código se lee con la cámara (html5-qrcode) o con una pistola lectora,
 * que escribe el código en el campo como si fuera un teclado.
 */
@Component({
  selector: 'app-recepcion',
  templateUrl: './recepcion.html',
})
export class Recepcion implements OnInit, OnDestroy {
  private readonly api = inject(ApiService);
  private readonly avisos = inject(AvisosService);
  private readonly router = inject(Router);
  protected readonly tono = tono;

  /** ?codigo=OC-... en la URL (al venir desde el detalle de la orden). */
  readonly codigo = input<string | undefined>();

  protected readonly orden = signal<OrdenCompra | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly camaraActiva = signal(false);
  protected readonly errorCamara = signal<string | null>(null);
  protected recibido: Record<number, number> = {};
  protected observacion = '';
  private escaner: Html5Qrcode | null = null;

  ngOnInit(): void {
    if (this.codigo()) {
      this.buscar(this.codigo()!);
    }
  }

  ngOnDestroy(): void {
    this.detenerCamara();
  }

  buscar(codigo: string): void {
    const limpio = codigo.trim().toUpperCase();
    this.orden.set(null);
    this.aviso.set(null);
    if (!limpio) {
      return;
    }
    this.api.ordenPorCodigo(limpio).subscribe({
      next: (o) => {
        this.orden.set(o);
        this.recibido = {};
        o.items.forEach((it) => (this.recibido[it.id] = it.disponible === false ? 0 : it.cantidad));
        this.observacion = '';
        if (!o.recibible) {
          this.aviso.set(`La orden está en estado "${o.estado}". Solo se reciben órdenes confirmadas por el proveedor.`);
        }
      },
      error: (e) => this.aviso.set(e.error?.mensaje ?? 'No se encontró la orden'),
    });
  }

  registrar(): void {
    const o = this.orden();
    if (!o) {
      return;
    }
    this.api.recibirOrden(o.id, this.recibido, this.observacion).subscribe({
      next: (r) => {
        if (r.estado === 'Recibida') {
          this.avisos.exito(`Recepción registrada: stock actualizado y orden ${r.codigo} cerrada.`);
        } else {
          this.avisos.error('Recepción registrada CON INCIDENCIA: revisa la línea de tiempo para reclamar al proveedor.');
        }
        this.router.navigate(['/compras', r.id]);
      },
      error: (e) => this.avisos.errorHttp(e),
    });
  }

  alternarCamara(campo: HTMLInputElement): void {
    if (this.escaner) {
      this.detenerCamara();
      return;
    }
    this.errorCamara.set(null);
    this.camaraActiva.set(true);
    // Espera a que Angular dibuje el contenedor #lector antes de abrir la cámara.
    setTimeout(() => {
      this.escaner = new Html5Qrcode('lector');
      this.escaner.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: { width: 220, height: 220 } },
        (texto) => {
          this.detenerCamara();
          campo.value = texto.trim();
          this.buscar(texto);
        },
        () => { /* sin código en este cuadro: seguir intentando */ },
      ).catch((err) => {
        this.detenerCamara();
        this.errorCamara.set(`No se pudo abrir la cámara (${err}). Escribe el código o usa una pistola lectora.`);
      });
    });
  }

  private detenerCamara(): void {
    if (this.escaner) {
      this.escaner.stop().catch(() => { /* ya estaba detenido */ });
      this.escaner = null;
    }
    this.camaraActiva.set(false);
  }
}
