export class Header {
    constructor(parent, createSearch, profileIconPath) {
        this.parent = parent;
        this.createSearch = createSearch;
        this.profileIconPath = profileIconPath;

        this.init();
    }

    init() {
        const header = document.createElement('header');
        header.classList.add('header');
        this.parent.appendChild(header);

        const searchInput = this.createSearch(header);
        searchInput.getElement().classList.add('header__search-input');

        const profileButton = document.createElement('button');
        profileButton.type = 'button';
        profileButton.classList.add('header__profile-button');
        profileButton.innerHTML = `<img src="${this.profileIconPath}" alt="Профиль" class="header__profile-icon">`;
        header.appendChild(profileButton);

        this.parent.appendChild(header);
    }
}