# Topface для Android

## Начало работы
Для начала разработки проекта нужно выполнитель следующие команды

    git checkout develop # Начинаем разработку с последней девелоперской версии
    git submodule init && git submodule update # Подтягиваем подмодули

## Сборка
Вся сборка проекта осуществляется через [Gradle](http://tools.android.com/tech-docs/new-build-system/user-guide)
Соответственно нужно его [устновить](http://www.gradle.org/downloads) и/или заиметь IDE с его нативной поддержкой

Для сборки через gradle должна быть устновлена переменная окружения ANDROID_HOME или свойство sdk.dir в файле local.properties (его нужно создать в корне проекта)
Для первоначальной сборки достаточно выполнить (будут собраны как debug версия, так и подписаный release) 

    gradle build

Возможно и установка сразу на устройство

    gradle installDebug #Собрать и установить debug версию

## Интеграция с Idea
Для интеграции с Idea есть два способа
* Установить Android Studio (Idea 13 EAP пока не работает с таким способом) и импортировать как Gradle проект
* При сборке генерятся конфиги для Idea, поэтому можно открыть любой версией проект со всеми зависимостями

## Ключ для подписи APK
Ключ для подписи apk файла (необходим для загрузки в Android Market), но проще собирать release через Gradle, там все конфиги сборки уже настроены: 
misc/topface-apk.key

    логин: topface
    пароль: Sonetica2012
