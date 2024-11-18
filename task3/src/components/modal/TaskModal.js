export class TaskModal {
    constructor() {
        this.onClose = null;
        this.init();
    }

    init() {
        this.overlay = document.createElement('div');
        this.overlay.className = 'modal-overlay';

        this.modal = document.createElement('div');
        this.modal.className = 'modal';

        this.content = document.createElement('div');
        this.content.className = 'modal__content';

        this.title = document.createElement('h2');
        this.title.className = 'modal__title';

        this.date = document.createElement('div');
        this.date.className = 'modal__date';

        this.checkbox = document.createElement('input');
        this.checkbox.type = 'checkbox';
        this.checkbox.className = 'modal__checkbox';

        this.description = document.createElement('div');
        this.description.className = 'modal__description';

        const closeButton = document.createElement('button');
        closeButton.className = 'modal__close-button';
        closeButton.textContent = 'Готово';

        this.content.append(
            this.title,
            this.date,
            this.checkbox,
            this.description,
            closeButton
        );

        this.modal.appendChild(this.content);
        this.overlay.appendChild(this.modal);

        closeButton.addEventListener('click', () => this.close());
        this.overlay.addEventListener('click', (e) => {
            if (e.target === this.overlay) {
                this.close();
            }
        });

        document.body.appendChild(this.overlay);
        this.overlay.style.display = 'none';
    }

    show(task) {
        this.title.textContent = task.name;
        this.date.textContent = new Date(task.date).toLocaleString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
        this.checkbox.checked = task.status;
        this.description.textContent = task.fullDesc;
        this.overlay.style.display = 'flex';
    }

    close() {
        this.overlay.style.display = 'none';
        if (this.onClose) {
            this.onClose();
        }
    }

    setOnClose(callback) {
        this.onClose = callback;
    }
}