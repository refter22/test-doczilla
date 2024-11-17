export class App {
    constructor(root, createHeader, createSidebar, content) {
        this.root = root;
        this.createHeader = createHeader;
        this.createSidebar = createSidebar;
        this.content = content;

        this.init();
    }

    init() {
        const header = this.createHeader(this.root);

        const main = document.createElement('main');
        this.createSidebar(main);

        this.root.appendChild(main);
        main.appendChild(this.content);
    }
}