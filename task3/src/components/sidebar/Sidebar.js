export class Sidebar {
    constructor(parent, createCalendar) {
        this.parent = parent;
        this.createCalendar = createCalendar;
        this.init();
    }

    init() {
        const sidebar = document.createElement('aside');
        sidebar.className = 'sidebar';

        const todayButton = document.createElement('button');
        todayButton.className = 'sidebar__filter-button';
        todayButton.textContent = 'Сегодня';

        const weekButton = document.createElement('button');
        weekButton.className = 'sidebar__filter-button';
        weekButton.textContent = 'На неделю';

        sidebar.appendChild(todayButton);
        sidebar.appendChild(weekButton);

        this.createCalendar(sidebar);

        this.parent.appendChild(sidebar);

        this.element = sidebar;
    }

    getElement() {
        return this.element;
    }
}
