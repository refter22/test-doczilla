export class Sidebar {
    constructor(parent, createCalendar) {
        this.parent = parent;
        this.createCalendar = createCalendar;
        this.onFilterChange = null;
        this.activeFilter = null;
        this.buttons = {};
        this.init();
    }

    init() {
        const sidebar = document.createElement('aside');
        sidebar.className = 'sidebar';

        this.buttons.today = document.createElement('button');
        this.buttons.today.className = 'sidebar__filter-button';
        this.buttons.today.textContent = 'Сегодня';
        this.buttons.today.addEventListener('click', () => {
            console.log('Today button clicked');
            if (this.onFilterChange) {
                const today = new Date();
                today.setHours(0, 0, 0, 0);
                this.setActiveFilter('today');
                this.onFilterChange('today', today);
            }
        });

        this.buttons.week = document.createElement('button');
        this.buttons.week.className = 'sidebar__filter-button';
        this.buttons.week.textContent = 'На неделю';
        this.buttons.week.addEventListener('click', () => {
            console.log('Week button clicked');
            if (this.onFilterChange) {
                const today = new Date();
                today.setHours(0, 0, 0, 0);
                const weekEnd = new Date(today);
                weekEnd.setDate(weekEnd.getDate() + 6);
                weekEnd.setHours(23, 59, 59, 999);
                this.setActiveFilter('week');
                this.onFilterChange('week', today, weekEnd);
            }
        });

        sidebar.appendChild(this.buttons.today);
        sidebar.appendChild(this.buttons.week);

        this.createCalendar(sidebar);
        this.parent.appendChild(sidebar);
        this.element = sidebar;
    }

    setActiveFilter(filterType) {
        console.log('Setting active filter:', filterType);
        console.log('Current buttons:', this.buttons);

        Object.values(this.buttons).forEach(button => {
            console.log('Removing active class from button:', button);
            button.classList.remove('sidebar__filter-button--active');
        });

        if (filterType && this.buttons[filterType]) {
            console.log('Adding active class to button:', this.buttons[filterType]);
            this.buttons[filterType].classList.add('sidebar__filter-button--active');
        }

        this.activeFilter = filterType;

        Object.entries(this.buttons).forEach(([key, button]) => {
            console.log(`Button ${key} classes:`, button.classList.toString());
        });
    }

    setOnFilterChange(callback) {
        this.onFilterChange = callback;
    }

    getElement() {
        return this.element;
    }

    resetActiveFilter() {
        this.setActiveFilter(null);
    }
}
