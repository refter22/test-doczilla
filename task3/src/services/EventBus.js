export class EventBus {
    constructor() {
        this.listeners = {};
    }

    on(event, callback) {
        if (!this.listeners[event]) {
            this.listeners[event] = [];
        }
        this.listeners[event].push(callback);
        console.log(`Subscribed to ${event}`, this.listeners);
    }

    off(event, callback) {
        if (!this.listeners[event]) return;
        this.listeners[event] = this.listeners[event].filter(cb => cb !== callback);
    }

    emit(event, data) {
        console.log(`Emitting ${event}`, data, this.listeners);
        if (!this.listeners[event]) return;
        this.listeners[event].forEach(callback => callback(data));
    }
}