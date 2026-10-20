// Recepción: lee el QR / código de barras de la orden con la cámara (HU-08).
// Usa la librería html5-qrcode. Al leer un código, lo pone en el campo y busca la orden.
(function () {
    'use strict';

    var boton = document.getElementById('btnCamara');
    var lector = document.getElementById('lector');
    var aviso = document.getElementById('errorCamara');
    if (!boton || !window.Html5Qrcode) {
        return;
    }
    var escaner = null;

    function mostrarError(texto) {
        aviso.textContent = texto;
        aviso.classList.remove('d-none');
    }

    function detener() {
        if (escaner) {
            escaner.stop().catch(function () { /* ya estaba detenido */ });
            escaner = null;
        }
        lector.classList.add('d-none');
        boton.innerHTML = '<i class="bi bi-camera me-1"></i>Escanear con la cámara';
    }

    boton.addEventListener('click', function () {
        if (escaner) {
            detener();
            return;
        }
        aviso.classList.add('d-none');
        lector.classList.remove('d-none');
        boton.innerHTML = '<i class="bi bi-x-lg me-1"></i>Detener cámara';
        escaner = new Html5Qrcode('lector');
        escaner.start(
            { facingMode: 'environment' },
            { fps: 10, qrbox: { width: 220, height: 220 } },
            function (texto) {
                detener();
                document.getElementById('codigo').value = texto.trim();
                document.getElementById('formCodigo').submit();
            },
            function () { /* sin código en este cuadro: seguir intentando */ }
        ).catch(function (err) {
            detener();
            mostrarError('No se pudo abrir la cámara (' + err + '). Escribe el código o usa una pistola lectora.');
        });
    });
})();
