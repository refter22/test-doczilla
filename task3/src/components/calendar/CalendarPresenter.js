export class CalendarPresenter {
    constructor(view, tasksService, eventBus) {
        this.view = view;
        this.tasksService = tasksService;
        this.eventBus = eventBus;

        this.init();
    }

    init() {
        this.view.setOnDateRangeSelect((startDate, endDate) => {
            this.handleDateRangeSelect(startDate, endDate);
        });

        this.eventBus.on('resetCalendarDates', () => {
            this.view.resetDates();
        });
    }

    handleDateRangeSelect(startDate, endDate) {
        this.eventBus.emit('dateRangeSelected', { startDate, endDate });
    }
}