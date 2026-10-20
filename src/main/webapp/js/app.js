// Comportamiento de la interfaz: íconos, menú móvil, modales y confirmaciones.
// Todo el trabajo con datos lo hacen los servlets; aquí solo hay UI.
document.addEventListener('DOMContentLoaded', function () {
    // Íconos lucide (<i data-lucide="nombre">)
    if (window.lucide) {
        window.lucide.createIcons();
    }

    // Menú lateral en móvil
    document.querySelectorAll('[data-menu]').forEach(function (el) {
        el.addEventListener('click', function () {
            document.body.classList.toggle('menu-abierto');
        });
    });

    // Abrir un modal: <button data-abrir="idDelDialog">
    // Los atributos data-campo-xxx rellenan el campo "xxx" del formulario del modal.
    document.querySelectorAll('[data-abrir]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var modal = document.getElementById(btn.dataset.abrir);
            var form = modal.querySelector('form');
            if (btn.hasAttribute('data-limpiar')) {
                form.reset();
                form.querySelectorAll('input[type=hidden][data-limpiable]').forEach(function (h) { h.value = ''; });
            }
            Object.keys(btn.dataset).forEach(function (clave) {
                if (clave.indexOf('campo') === 0) {
                    var nombre = clave.substring(5).replace(/^./, function (c) { return c.toLowerCase(); })
                        .replace(/[A-Z]/g, function (c) { return '_' + c.toLowerCase(); });
                    var campo = form.elements[nombre];
                    if (campo) {
                        campo.value = btn.dataset[clave];
                    }
                }
            });
            if (btn.dataset.titulo) {
                modal.querySelector('[data-titulo-modal]').textContent = btn.dataset.titulo;
            }
            if (btn.dataset.boton) {
                modal.querySelector('[data-boton-modal]').textContent = btn.dataset.boton;
            }
            modal.showModal();
            var primero = form.querySelector('input:not([type=hidden]), select');
            if (primero) {
                primero.focus();
            }
        });
    });

    document.querySelectorAll('[data-cerrar]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            btn.closest('dialog').close();
        });
    });

    // Cerrar el modal al hacer clic fuera de él
    document.querySelectorAll('dialog.modal').forEach(function (d) {
        d.addEventListener('click', function (e) {
            if (e.target === d) {
                d.close();
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
});
