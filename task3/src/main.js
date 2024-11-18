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

const root = document.getElementById('app')

const profileIconPath = './assets/icons/profile.svg'
const searchIconPath = './assets/icons/search.svg'

const createSearchInput = (parent) => {
    const search = new Search(parent, searchIconPath);
    const searchPresenter = new SearchPresenter(search, tasksService, eventBus);
    return search;
};

const createHeader = (parent) =>
    new Header(parent, createSearchInput, profileIconPath)

const tasksService = new TasksService("http://localhost:3000/api");
const eventBus = new EventBus();

// TasksList
const content = document.createElement('content');
const tasksList = new TasksList(content);
const tasksPresenter = new TasksListPresenter(tasksList, tasksService, eventBus);

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
