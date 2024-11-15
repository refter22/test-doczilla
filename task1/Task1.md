# **Задание #1**

Имеется корневая папка. В этой папке могут находиться текстовые файлы, а также другие папки. В других папках также могут находится текстовые файлы и папки (уровень вложенности может оказаться любым). Найти все текстовые файлы, отсортировать их по имени и склеить содержимое в один текстовый файл.

В каждом файле может быть ни одной, одна или несколько директив формата:

*require ‘<путь к другому файлу от корневого каталога>’*

Директива означает, что текущий файл зависит от другого указанного файла. Необходимо выявить все зависимости между файлами, построить сортированный список, для которого выполняется условие: если файл А, зависит от файла В, то файл А находится ниже файла В в списке. Осуществить конкатенацию файлов в соответствии со списком. Если такой список построить невозможно (существует циклическая зависимость), программа должна вывести соответствующее сообщение. В случае циклической зависимости вывести объяснение ошибки - указать цикл зависимостей между файлами.

☝ **Реализация должна быть написана на любом из следующих языков: C, C++, Java, Kotlin, Rust**

➡️ Пример. Дана структура файлов и каталогов:

- Каталог “Folder 1”
    - Файл “File 1-1”. Содержимое:

    `Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse id enim euismod erat elementum cursus. In hac habitasse platea dictumst. Etiam vitae tortor ipsum. Morbi massa augue, lacinia sed nisl id, congue eleifend lorem.`

    **`*require ‘Folder 2/File 2-1’*`**

    `Praesent feugiat egestas sem, id luctus lectus dignissim ac. Donec elementum rhoncus quam, vitae viverra massa euismod a. Morbi dictum sapien sed porta tristique. Donec varius convallis quam in fringilla.`

- Каталог “Folder 2”
    - Файл “File 2-1”. Содержимое:

    `Phasellus eget tellus ac risus iaculis feugiat nec in eros. Aenean in luctus ante. In lacinia lectus tempus, rutrum ipsum quis, gravida nunc. Fusce tempor eleifend libero at pharetra. Nulla lacinia ante ac felis malesuada auctor. Vestibulum eget congue sapien, ac euismod elit. Fusce nisl ante, consequat et imperdiet vel, semper et neque.`

    - Файл “File 2-2”. Содержимое:

    **`*require ‘Folder 1/File 1-1’*`**

    **`*require ‘Folder 2/File 2-1’*`**

    `In pretium dictum lacinia. In rutrum, neque a dignissim maximus, dolor mi pretium ante, nec volutpat justo dolor non nulla. Vivamus nec suscipit nisl, ornare luctus erat. Aliquam eget est orci. Proin orci urna, elementum a nunc ac, fermentum varius eros. Mauris id massa elit.`


💡 Для указанной структуры каталогов и файлов должен быть построен список:

Folder 2/File 2-1

Folder 1/File 1-1

Folder 2/File 2-2