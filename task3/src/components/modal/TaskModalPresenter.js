export class TaskModalPresenter {
    constructor(view, eventBus) {
        this.view = view;
        this.eventBus = eventBus;
        this.init();
    }

    init() {
        this.eventBus.on('taskSelected', (task) => {
            this.view.show(task);
        });

        this.view.setOnClose(() => {
            this.eventBus.emit('modalClosed');
        });
    }
}