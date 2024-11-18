export class SidebarPresenter {
    constructor(view, eventBus) {
        this.view = view;
        this.eventBus = eventBus;
        this.init();
    }

    init() {
        this.view.setOnFilterChange((filterType, startDate, endDate) => {
            this.eventBus.emit('resetCalendarDates');

            if (filterType === 'today') {
                this.eventBus.emit('dateRangeSelected', {
                    startDate,
                    endDate: startDate,
                    source: 'button'
                });
            } else if (filterType === 'week') {
                this.eventBus.emit('dateRangeSelected', {
                    startDate,
                    endDate,
                    source: 'button'
                });
            }
        });

        this.eventBus.on('dateRangeSelected', (data) => {
            if (data.source !== 'button') {
                this.view.resetActiveFilter();
            }
        });
    }
}