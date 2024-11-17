import { App } from './App.js'
import { Header } from './components/header/Header.js'
import { Search } from './components/search/Search.js'
import { Sidebar } from './components/sidebar/Sidebar.js'
import { Calendar } from './components/calendar/Calendar.js'
import { TasksList } from './components/tasksList/TasksList.js'

const root = document.getElementById('app')

const profileIconPath = './assets/icons/profile.svg'
const searchIconPath = './assets/icons/search.svg'

const createHeader = (parent) =>
  new Header(parent, createSearchInput, profileIconPath)

const createSearchInput = (parent) => new Search(parent, searchIconPath)

const createCalendar = (parent) => new Calendar(parent)

const createSidebar = (parent) => new Sidebar(parent, createCalendar)

const createTasksList = (parent) => new TasksList(parent)
const content = document.createElement('content')
createTasksList(content);

const app = new App(root, createHeader, createSidebar, content)
