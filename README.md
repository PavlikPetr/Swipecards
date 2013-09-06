# Topface для Android

## Начало работы
Для начала разработки проекта нужно выполнитель следующие команды

    git checkout develop # Начинаем разработку с последней девелоперской версии
    git submodule init && git submodule update # Подтягиваем подмодули

## Сборка
Вся сборка проекта осуществляется через [Gradle](http://tools.android.com/tech-docs/new-build-system/user-guide)
Соответственно нужно его [устновить](http://www.gradle.org/downloads) и/или заиметь IDE с его нативной поддержкой

Для первоначальной сборки и последующей установки полученого билда на устройство достаточно выполнить

    gradle installDebug

## Ключ для подписи APK
Ключ для подписи apk файла (необходим для загрузки в Android Market): 
topface-apk.key

    логин: topface
    пароль: Sonetica2012
