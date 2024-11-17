export class Search {
    constructor(parent, searchIconPath) {
        this.parent = parent;
        this.searchIconPath = searchIconPath;
        this.init();
    }

    init() {
        const search = document.createElement('div');
        search.className = 'search';

        const searchIcon = document.createElement('img');
        searchIcon.src = this.searchIconPath;
        searchIcon.className = 'search__icon';

        const searchInput = document.createElement('input');
        searchInput.type = 'text';
        searchInput.placeholder = 'Поиск';
        searchInput.className = 'search__input';

        const dropdown = document.createElement('div');
        dropdown.className = 'search__dropdown';

        for (let i = 0; i < 3; i++) {
            const item = document.createElement('div');
            item.className = 'search__dropdown-item';
            item.textContent = 'Название';
            dropdown.appendChild(item);
        }

        search.appendChild(searchIcon);
        search.appendChild(searchInput);
        search.appendChild(dropdown);

        search.addEventListener('click', () => {
            searchInput.focus();
        });

        this.parent.appendChild(search);
        this.searchElement = search;
    }

    getElement() {
        return this.searchElement;
    }
}