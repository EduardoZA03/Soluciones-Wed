// Comportamiento de la interfaz sobre Bootstrap 5: rellenar modales,
// confirmaciones y selects que guardan solos.
// Todo el trabajo con datos lo hacen los servlets; aquí solo hay UI.
(function () {
    'use strict';

    // Al abrir un modal con un botón (data-bs-toggle="modal"):
    //  - data-limpiar        → vacía el formulario (modo "nuevo")
    //  - data-campo-xxx="v"  → pone v en el campo "xxx" (modo "editar")
    //  - data-titulo / data-boton → cambia el título y el texto del botón
    document.querySelectorAll('.modal').forEach(function (modal) {
        modal.addEventListener('show.bs.modal', function (e) {
            var btn = e.relatedTarget;
            var form = modal.querySelector('form');
            if (!btn || !form) {
                return;
            }
            if (btn.hasAttribute('data-limpiar')) {
                form.reset();
                form.querySelectorAll('input[data-limpiable]').forEach(function (h) { h.value = ''; });
            }
            Object.keys(btn.dataset).forEach(function (clave) {
                if (clave.indexOf('campo') !== 0) {
                    return;
                }
                // campoStockMinimo → stock_minimo
                var nombre = clave.substring(5)
                    .replace(/^./, function (c) { return c.toLowerCase(); })
                    .replace(/[A-Z]/g, function (c) { return '_' + c.toLowerCase(); });
                if (form.elements[nombre]) {
                    form.elements[nombre].value = btn.dataset[clave];
                }
            });
            // Casillas: data-activo="true|false" marca o desmarca el checkbox "activo"
            if (btn.dataset.activo && form.elements.activo) {
                form.elements.activo.checked = btn.dataset.activo === 'true';
            }
            // Campos que solo aplican al crear (.solo-nuevo) o al editar (.solo-edicion)
            var editando = btn.hasAttribute('data-campo-id') && !btn.hasAttribute('data-limpiar');
            modal.querySelectorAll('.solo-nuevo').forEach(function (el) {
                el.classList.toggle('d-none', editando);
                el.querySelectorAll('input, select').forEach(function (i) { i.disabled = editando; });
            });
            modal.querySelectorAll('.solo-edicion').forEach(function (el) {
                el.classList.toggle('d-none', !editando);
            });
            if (btn.dataset.titulo) {
                modal.querySelector('.modal-title').textContent = btn.dataset.titulo;
            }
            if (btn.dataset.boton) {
                modal.querySelector('[type=submit]').textContent = btn.dataset.boton;
            }
        });

        // Pone el cursor en el primer campo cuando el modal ya se ve
        modal.addEventListener('shown.bs.modal', function () {
            var primero = modal.querySelector('input:not([type=hidden]), select');
            if (primero) {
                primero.focus();
            }
        });
    });

    // Confirmación antes de enviar: <form data-confirmar="¿Seguro?">
    document.querySelectorAll('form[data-confirmar]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            if (!window.confirm(form.dataset.confirmar)) {
                e.preventDefault();
            }
        });
    });

    // Selects que guardan al cambiar (estado de reserva, rol de usuario)
    document.querySelectorAll('select[data-autoenvio]').forEach(function (sel) {
        sel.addEventListener('change', function () {
            sel.form.submit();
        });
    });
})();
