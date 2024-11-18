export class TasksList {
    constructor(parent) {
        this.parent = parent;
        this.tasks = [];
        this.total = 0;
        this.page = 1;
        this.limit = 5;
        this.onPageChange = null;
        this.init();
    }

    init() {
        const container = document.createElement('div');
        container.classList.add('tasks-list');

        const header = document.createElement('div');
        header.classList.add('tasks-list__header');

        const date = document.createElement('div');
        date.textContent = new Date().toLocaleDateString('ru-RU', {
            day: 'numeric',
            month: 'long',
            year: 'numeric'
        });

        const sort = document.createElement('div');
        sort.classList.add('tasks-list__sort');
        sort.innerHTML = `
            <span>Сортировать по дате</span>
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M3.5 5.5L8 10L12.5 5.5" stroke="currentColor" stroke-width="1.5"/>
            </svg>
        `;

        header.append(date, sort);
        container.appendChild(header);

        this.tasksContainer = document.createElement('div');
        this.tasksContainer.classList.add('tasks-list__items');
        container.appendChild(this.tasksContainer);

        this.paginationContainer = document.createElement('div');
        this.paginationContainer.classList.add('tasks-list__pagination');
        container.appendChild(this.paginationContainer);

        this.parent.appendChild(container);
    }

    setTasks(tasks, total, page, limit) {
        this.tasks = tasks;
        this.total = total;
        this.page = page;
        this.limit = limit;
        this.renderTasks();
        this.renderPagination();
    }

    setOnPageChange(callback) {
        this.onPageChange = callback;
    }

    renderPagination() {
        this.paginationContainer.innerHTML = '';

        const totalPages = Math.ceil(this.total / this.limit);

        const prevButton = document.createElement('button');
        prevButton.classList.add('pagination-button');
        prevButton.textContent = 'Назад';
        prevButton.disabled = this.page === 1;
        prevButton.addEventListener('click', () => {
            if (this.page > 1 && this.onPageChange) {
                this.onPageChange(this.page - 1);
            }
        });

        const pageInfo = document.createElement('span');
        pageInfo.classList.add('pagination-info');
        pageInfo.textContent = `${this.page} из ${totalPages}`;

        const nextButton = document.createElement('button');
        nextButton.classList.add('pagination-button');
        nextButton.textContent = 'Вперед';
        nextButton.disabled = this.page >= totalPages;
        nextButton.addEventListener('click', () => {
            if (this.page < totalPages && this.onPageChange) {
                this.onPageChange(this.page + 1);
            }
        });

        this.paginationContainer.append(prevButton, pageInfo, nextButton);
    }

    renderTasks() {
        this.tasksContainer.innerHTML = '';

        this.tasks.forEach(task => {
            const taskElement = document.createElement('div');
            taskElement.classList.add('task-item');

            const content = document.createElement('div');
            content.classList.add('task-item__content');

            const title = document.createElement('div');
            title.classList.add('task-item__title');
            title.textContent = task.name;

            const description = document.createElement('div');
            description.classList.add('task-item__description');
            description.textContent = task.shortDesc;

            content.append(title, description);

            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.checked = task.status;
            checkbox.classList.add('task-item__checkbox');

            checkbox.addEventListener('change', (e) => {
                e.preventDefault();
                checkbox.checked = task.status;
            });

            const time = document.createElement('div');
            time.classList.add('task-item__time');
            time.textContent = new Date(task.date).toLocaleString('ru-RU', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });

            taskElement.append(content, checkbox, time);
            this.tasksContainer.appendChild(taskElement);
        });
    }
}