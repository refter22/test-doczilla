export class Calendar {
    constructor(parent) {
        this.parent = parent;
        this.currentDate = new Date();
        this.primaryDate = null;
        this.secondaryDate = null;
        this.calendar = null;
        this.titleElement = null;
        this.gridElement = null;
        this.init();
    }

    init() {
        this.calendar = document.createElement('div');
        this.calendar.classList.add('calendar');

        const header = this.createHeader();
        this.calendar.appendChild(header);

        this.gridElement = this.createGrid();
        this.calendar.appendChild(this.gridElement);

        this.parent.appendChild(this.calendar);
    }

    createHeader() {
        const header = document.createElement('div');
        header.classList.add('calendar__header');

        const prevButton = document.createElement('button');
        prevButton.classList.add('calendar__nav-button');
        prevButton.innerHTML = '◀';
        prevButton.onclick = () => this.changeMonth(-1);

        this.titleElement = document.createElement('div');
        this.titleElement.textContent = this.formatMonthYear();

        const nextButton = document.createElement('button');
        nextButton.classList.add('calendar__nav-button');
        nextButton.innerHTML = '▶';
        nextButton.onclick = () => this.changeMonth(1);

        header.append(prevButton, this.titleElement, nextButton);
        return header;
    }

    createGrid() {
        const grid = document.createElement('div');
        grid.classList.add('calendar__grid');

        const weekdays = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
        weekdays.forEach(day => {
            const weekdayCell = document.createElement('div');
            weekdayCell.classList.add('calendar__weekday');
            weekdayCell.textContent = day;
            grid.appendChild(weekdayCell);
        });

        this.renderDays(grid);

        return grid;
    }

    renderDays(grid) {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);

        for (let i = 0; i < firstDay.getDay(); i++) {
            const emptyDay = document.createElement('div');
            emptyDay.classList.add('calendar__day');
            grid.appendChild(emptyDay);
        }

        for (let day = 1; day <= lastDay.getDate(); day++) {
            const dayElement = document.createElement('div');
            dayElement.classList.add('calendar__day');
            dayElement.textContent = day;

            const currentDate = new Date(year, month, day);

            if (this.primaryDate && this.isSameDate(currentDate, this.primaryDate)) {
                dayElement.classList.add('calendar__day--primary');
            } else if (this.secondaryDate && this.isSameDate(currentDate, this.secondaryDate)) {
                dayElement.classList.add('calendar__day--secondary');
            }

            dayElement.onclick = () => this.selectDate(currentDate);
            grid.appendChild(dayElement);
        }
    }

    isSameDate(date1, date2) {
        return date1.getDate() === date2.getDate() &&
               date1.getMonth() === date2.getMonth() &&
               date1.getFullYear() === date2.getFullYear();
    }

    changeMonth(delta) {
        this.currentDate.setMonth(this.currentDate.getMonth() + delta);
        this.titleElement.textContent = this.formatMonthYear();

        while (this.gridElement.firstChild) {
            this.gridElement.removeChild(this.gridElement.firstChild);
        }

        const weekdays = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
        weekdays.forEach(day => {
            const weekday = document.createElement('div');
            weekday.classList.add('calendar__weekday');
            weekday.textContent = day;
            this.gridElement.appendChild(weekday);
        });

        this.renderDays(this.gridElement);
    }

    formatMonthYear() {
        return this.currentDate.toLocaleString('default', { month: 'long', year: 'numeric' });
    }

    selectDate(date) {
        const isSameMonth = (date1, date2) => {
            return date1.getMonth() === date2.getMonth() &&
                   date1.getFullYear() === date2.getFullYear();
        };

        if ((this.primaryDate && !isSameMonth(date, this.primaryDate)) ||
            (this.secondaryDate && !isSameMonth(date, this.secondaryDate))) {
            this.primaryDate = null;
            this.secondaryDate = null;
        }

        if (!this.primaryDate) {
            this.primaryDate = date;
        } else if (this.isSameDate(date, this.primaryDate) && !this.secondaryDate) {
            this.primaryDate = null;
        } else if (this.isSameDate(date, this.primaryDate) && this.secondaryDate) {
            this.primaryDate = date;
            this.secondaryDate = null;
        } else if (this.secondaryDate && this.isSameDate(date, this.secondaryDate)) {
            this.primaryDate = date;
            this.secondaryDate = null;
        } else {
            this.secondaryDate = this.primaryDate;
            this.primaryDate = date;
        }

        while (this.gridElement.firstChild) {
            this.gridElement.removeChild(this.gridElement.firstChild);
        }

        const weekdays = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
        weekdays.forEach(day => {
            const weekday = document.createElement('div');
            weekday.classList.add('calendar__weekday');
            weekday.textContent = day;
            this.gridElement.appendChild(weekday);
        });

        this.renderDays(this.gridElement);
    }
}