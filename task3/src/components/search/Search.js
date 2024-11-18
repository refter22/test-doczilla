export class Search {
    constructor(parent, searchIconPath) {
        this.parent = parent;
        this.searchIconPath = searchIconPath;
        this.onSearch = null;
        this.searchTimeout = null;
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

        const resultsContainer = document.createElement('div');
        resultsContainer.className = 'search__results';

        const loadingState = document.createElement('div');
        loadingState.className = 'search__loading';
        loadingState.textContent = 'Поиск...';

        const errorState = document.createElement('div');
        errorState.className = 'search__error';
        errorState.textContent = 'Произошла ошибка';

        const noResultsState = document.createElement('div');
        noResultsState.className = 'search__no-results';
        noResultsState.textContent = 'Ничего не найдено';

        dropdown.appendChild(loadingState);
        dropdown.appendChild(errorState);
        dropdown.appendChild(noResultsState);
        dropdown.appendChild(resultsContainer);

        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.trim();

            if (this.searchTimeout) {
                clearTimeout(this.searchTimeout);
            }

            this.searchTimeout = setTimeout(() => {
                if (this.onSearch) {
                    this.onSearch(query);
                }
            }, 300);
        });

        search.appendChild(searchIcon);
        search.appendChild(searchInput);
        search.appendChild(dropdown);

        this.parent.appendChild(search);
        this.searchElement = search;
        this.dropdownElement = dropdown;
        this.resultsContainer = resultsContainer;
        this.loadingState = loadingState;
        this.errorState = errorState;
        this.noResultsState = noResultsState;
    }

    setLoading(isLoading) {
        this.loadingState.style.display = isLoading ? 'block' : 'none';
        this.resultsContainer.style.display = isLoading ? 'none' : 'block';
        this.noResultsState.style.display = 'none';
        this.errorState.style.display = 'none';
    }

    setError(error) {
        this.errorState.style.display = error ? 'block' : 'none';
        this.loadingState.style.display = 'none';
        this.resultsContainer.style.display = 'none';
        this.noResultsState.style.display = 'none';
        if (error) {
            this.errorState.textContent = error;
        }
    }

    setResults(results) {
        while (this.resultsContainer.firstChild) {
            this.resultsContainer.removeChild(this.resultsContainer.firstChild);
        }

        this.loadingState.style.display = 'none';
        this.errorState.style.display = 'none';

        if (!results || results.length === 0) {
            this.noResultsState.style.display = 'block';
            this.resultsContainer.style.display = 'none';
            return;
        }

        this.noResultsState.style.display = 'none';
        this.resultsContainer.style.display = 'block';

        results.forEach(result => {
            const item = document.createElement('div');
            item.className = 'search__dropdown-item';
            item.textContent = result.name;
            item.addEventListener('click', () => {
                if (this.onResultSelect) {
                    this.onResultSelect(result);
                }
            });
            this.resultsContainer.appendChild(item);
        });
    }

    setOnSearch(callback) {
        this.onSearch = callback;
    }

    getElement() {
        return this.searchElement;
    }
}