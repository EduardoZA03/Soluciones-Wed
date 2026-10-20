import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AvisosService } from './core/avisos.service';

/** Componente raíz: las páginas y los avisos flotantes de éxito / error. */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
})
export class App {
  protected readonly avisos = inject(AvisosService);
}
