export class TasksService {
    constructor(baseUrl = 'http://localhost:3000/api') {
        this.baseUrl = baseUrl;
    }

    async getTasks(page = 1, limit = 5) {
        const offset = (page - 1) * limit;

        const response = await fetch(`${this.baseUrl}/todos?limit=${limit}&offset=${offset}`);
        if (!response.ok) {
            throw new Error('Failed to fetch tasks');
        }
        return response.json();
    }

    async searchTasks(query, page = 1, limit = 5) {
        const offset = (page - 1) * limit;
        const response = await fetch(
            `${this.baseUrl}/todos/find?q=${encodeURIComponent(query)}&limit=${limit}&offset=${offset}`
        );
        if (!response.ok) {
            throw new Error('Failed to search tasks');
        }
        return response.json();
    }

    async getTasksByDateRange(startDate, endDate, page = 1, limit = 5) {
        const offset = (page - 1) * limit;
        const from = Math.floor(startDate.getTime());
        const to = Math.floor(endDate.getTime());

        const response = await fetch(
            `${this.baseUrl}/todos/date?from=${from}&to=${to}&limit=${limit}&offset=${offset}`
        );
        if (!response.ok) {
            throw new Error('Failed to fetch tasks by date range');
        }
        return response.json();
    }
}