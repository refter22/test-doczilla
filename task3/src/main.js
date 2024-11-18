import { App } from './App.js'
import { Header } from './components/header/Header.js'
import { Search } from './components/search/Search.js'
import { SearchPresenter } from './components/search/SearchPresenter.js'
import { Sidebar } from './components/sidebar/Sidebar.js'
import { Calendar } from './components/calendar/Calendar.js'
import { TasksList } from './components/tasksList/TasksList.js'
import { TasksListPresenter } from './components/tasksList/TasksListPresenter.js';
import { TasksService } from './services/TasksService/TasksService.js';
import { MockTasksService } from './services/TasksService/MockTasksService.js';
import { EventBus } from './services/EventBus.js';
import { CalendarPresenter } from './components/calendar/CalendarPresenter.js';
import { SidebarPresenter } from './components/sidebar/SidebarPresenter.js';
import { TaskModal } from './components/modal/TaskModal.js';
import { TaskModalPresenter } from './components/modal/TaskModalPresenter.js';

const root = document.getElementById('app')

const profileIconPath = './assets/icons/profile.svg'
const searchIconPath = './assets/icons/search.svg'

const eventBus = new EventBus();
const tasksService = new TasksService("http://localhost:3000/api");

// Создаем модальное окно
const taskModal = new TaskModal();
const taskModalPresenter = new TaskModalPresenter(taskModal, eventBus);

const createSearchInput = (parent) => {
    const search = new Search(parent, searchIconPath);
    const searchPresenter = new SearchPresenter(search, tasksService, eventBus);

    search.setOnResultClick((task) => {
        eventBus.emit('taskSelected', task);
    });

    return search;
};

const createHeader = (parent) =>
    new Header(parent, createSearchInput, profileIconPath)

// TasksList
const content = document.createElement('content');
const tasksList = new TasksList(content);
const tasksPresenter = new TasksListPresenter(tasksList, tasksService, eventBus);

tasksList.setOnTaskClick((task) => {
    eventBus.emit('taskSelected', task);
});

// Calendar
const sidebarContent = document.createElement('div');
const calendar = new Calendar(sidebarContent);
const calendarPresenter = new CalendarPresenter(calendar, tasksService, eventBus);

// Sidebar
const createCalendar = (parent) => {
    parent.appendChild(sidebarContent);
    return calendar;
};

const sidebar = new Sidebar(document.createElement('div'), createCalendar);
const sidebarPresenter = new SidebarPresenter(sidebar, eventBus);

const createSidebar = (parent) => {
    parent.appendChild(sidebar.getElement());
    return sidebar;
};

const app = new App(root, createHeader, createSidebar, content);
