// Mantém o aviso de exclusão em um recurso local permitido pela Content Security Policy.
document.querySelectorAll(".confirm-delete").forEach((form) => {
    form.addEventListener("submit", (event) => {
        if (!window.confirm(form.dataset.confirm || "Confirmar exclusão?")) {
            event.preventDefault();
        }
    });
});
