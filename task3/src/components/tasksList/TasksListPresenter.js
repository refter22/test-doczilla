export class TasksListPresenter {
    constructor(view, tasksService, eventBus) {
        console.log('TasksListPresenter: constructor');
        this.view = view;
        this.tasksService = tasksService;
        this.eventBus = eventBus;
        this.currentPage = 1;
        this.dateRange = null;

        this.init();
    }

    init() {
        console.log('TasksListPresenter: init');
        this.view.setOnPageChange(this.handlePageChange.bind(this));

        this.eventBus.on('dateRangeSelected', (data) => {
            console.log('TasksListPresenter: received dateRangeSelected event', data);
            this.handleDateRangeSelect(data);
        });

        this.loadTasks(this.currentPage);
    }

    async handleDateRangeSelect({ startDate, endDate }) {
        console.log('TasksListPresenter: handling date range', { startDate, endDate });

        if (startDate === null && endDate === null) {
            this.dateRange = null;
        } else {
            this.dateRange = { startDate, endDate };
        }

        this.currentPage = 1;
        await this.loadTasks(1);
    }

    async handlePageChange(page) {
        this.currentPage = page;
        await this.loadTasks(page);
    }

    async loadTasks(page = 1) {
        try {
            let response;
            if (this.dateRange) {
                console.log('TasksListPresenter: loading tasks with date range', this.dateRange);
                response = await this.tasksService.getTasksByDateRange(
                    this.dateRange.startDate,
                    this.dateRange.endDate,
                    page
                );
            } else {
                console.log('TasksListPresenter: loading all tasks');
                response = await this.tasksService.getTasks(page);
            }

            console.log('TasksListPresenter: received tasks', response);

            const tasks = Array.isArray(response) ? response : response.tasks;
            const total = response.total || tasks.length;
            const currentPage = response.page || page;
            const limit = response.limit || 5;

            this.view.setTasks(
                tasks,
                total,
                currentPage,
                limit
            );
        } catch (error) {
            console.error('Failed to load tasks:', error);
        }
    }
}