export class MockTasksService {
    constructor() {
        this.mockTasks = Array.from({ length: 25 }, (_, index) => {
            const date = new Date(2024, 2, 20 + Math.floor(index / 3));
            date.setHours(9 + Math.floor(Math.random() * 8));
            date.setMinutes(Math.floor(Math.random() * 60));

            return {
                id: `task-${index + 1}`,
                name: `Задача ${index + 1}`,
                shortDesc: `Краткое описание задачи ${index + 1}`,
                fullDesc: `Полное описание задачи ${index + 1}`,
                date: date.toISOString(),
                status: Math.random() > 0.5
            };
        });
    }

    async getTasks(page = 1, limit = 5) {
        await new Promise(resolve => setTimeout(resolve, 300));

        const start = (page - 1) * limit;
        const end = start + limit;
        const paginatedTasks = this.mockTasks.slice(start, end);

        return {
            tasks: paginatedTasks,
            total: this.mockTasks.length,
            page,
            limit
        };
    }

    async getTasksByDateRange(startDate, endDate) {
        await new Promise(resolve => setTimeout(resolve, 300));

        const start = new Date(startDate);
        start.setHours(0, 0, 0, 0);

        const end = new Date(endDate);
        end.setHours(23, 59, 59, 999);

        const filteredTasks = this.mockTasks.filter(task => {
            const taskDate = new Date(task.date);
            return taskDate >= start && taskDate <= end;
        });

        return {
            tasks: filteredTasks,
            total: filteredTasks.length,
            page: 1,
            limit: filteredTasks.length
        };
    }

    async findTasks(query) {
        await new Promise(resolve => setTimeout(resolve, 300));

        const filteredTasks = this.mockTasks.filter(task =>
            task.name.toLowerCase().includes(query.toLowerCase())
        );

        return {
            tasks: filteredTasks,
            total: filteredTasks.length,
            page: 1,
            limit: filteredTasks.length
        };
    }

    async searchTasks(query, page = 1, limit = 5) {
        await new Promise(resolve => setTimeout(resolve, 500));

        const filteredTasks = this.mockTasks.filter(task =>
            task.name.toLowerCase().includes(query.toLowerCase()) ||
            task.shortDesc.toLowerCase().includes(query.toLowerCase())
        );

        const start = (page - 1) * limit;
        const end = start + limit;

        return {
            tasks: filteredTasks.slice(start, end),
            total: filteredTasks.length,
            page,
            limit
        };
    }
}