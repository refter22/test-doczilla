export class TasksList {
    constructor(parent) {
        this.parent = parent;
        this.tasks = [
            {
                id: 1,
                title: 'Название',
                description: 'Описание',
                completed: true,
                timestamp: '08.05.2022 00:10'
            },
            {
                id: 2,
                title: 'Название',
                description: 'Описание',
                completed: false,
                timestamp: '08.05.2022 00:05'
            },
            {
                id: 3,
                title: 'Название',
                description: 'Описание',
                completed: true,
                timestamp: '08.05.2022 00:00'
            }
        ];
        this.init();
    }

    init() {
        const container = document.createElement('div');
        container.classList.add('tasks-list');

        const header = document.createElement('div');
        header.classList.add('tasks-list__header');

        const date = document.createElement('div');
        date.textContent = '8 мая 2022';

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

        const tasksList = document.createElement('div');
        tasksList.classList.add('tasks-list__items');

        this.tasks.forEach(task => {
            const taskElement = document.createElement('div');
            taskElement.classList.add('task-item');

            const content = document.createElement('div');
            content.classList.add('task-item__content');

            const title = document.createElement('div');
            title.classList.add('task-item__title');
            title.textContent = task.title;

            const description = document.createElement('div');
            description.classList.add('task-item__description');
            description.textContent = task.description;

            content.append(title, description);

            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.checked = task.completed;
            checkbox.classList.add('task-item__checkbox');

            const time = document.createElement('div');
            time.classList.add('task-item__time');
            time.textContent = task.timestamp;

            taskElement.append(content, checkbox, time);
            tasksList.appendChild(taskElement);
        });

        container.appendChild(tasksList);
        this.parent.appendChild(container);
    }
}