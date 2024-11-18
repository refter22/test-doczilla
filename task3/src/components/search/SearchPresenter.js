export class SearchPresenter {
    constructor(view, tasksService, eventBus) {
        this.view = view;
        this.tasksService = tasksService;
        this.eventBus = eventBus;
        this.currentRequestId = 0;
        this.init();
    }

    init() {
        this.view.setOnSearch(this.handleSearch.bind(this));
    }

    async handleSearch(query) {
        const requestId = ++this.currentRequestId;
        this.view.setLoading(true);

        try {
            const response = await this.tasksService.searchTasks(query, 1, 3);
            console.log('Search response:', response);

            if (requestId === this.currentRequestId) {
                const results = Array.isArray(response) ? response : response.tasks;
                this.view.setResults(results);
            }
        } catch (error) {
            console.error('Search failed:', error);
            if (requestId === this.currentRequestId) {
                this.view.setError('Ошибка поиска');
            }
        } finally {
            if (requestId === this.currentRequestId) {
                this.view.setLoading(false);
            }
        }
    }
}